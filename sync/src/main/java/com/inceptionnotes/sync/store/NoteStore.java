package com.inceptionnotes.sync.store;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.AqlQueryOptions;
import com.google.gson.JsonArray;
import com.inceptionnotes.sync.Json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by jacob on 2/1/18.
 */

public class NoteStore {

    private static final AqlQueryOptions AQL_QUERY_OPTIONS = new AqlQueryOptions();

    private static final String AQL_PARAM_CLIENT = "client";
    private static final String AQL_PARAM_TOKEN = "token";
    private static final String AQL_PARAM_NOTE = "note";
    private static final String AQL_PARAM_EYE = "eye";
    private static final String AQL_PARAM_PERSON = "person";
    private static final String AQL_PARAM_PROP = "prop";
    private static final String AQL_PARAM_VALUE = "value";

    private static final String AQL_QUERY_CHANGES_FOR_CLIENT_VISIBLE_FROM_NOTE = "LET visible = APPEND(\n" +
            "    (FOR note IN entities FILTER note._id == @note RETURN note),\n" +
            "    (FOR entity, rel IN 1..2 OUTBOUND @note GRAPH 'graph'\n" +
            "        FILTER (rel.kind == 'item' OR rel.kind == 'ref')\n" + // (... OR (rel.kind == 'person' AND entity._id == @person))
            "        RETURN entity\n" +
            "    )\n" +
            ")\n" +
            "\n" +
            "FOR note IN visible FILTER note != null RETURN [\n" +
            "    note._key,\n" +
            "    (FOR prop IN entities FILTER prop.note == note._id AND prop.kind == 'prop' AND (\n" +
            "        FOR syncProp, sync IN OUTBOUND @client GRAPH 'state' FILTER syncProp == prop RETURN sync\n" +
            "    )[0].version != prop.version RETURN [\n" +
            "        prop._id,\n" +
            "        prop.type,\n" +
            "        prop.value\n" +
            "    ])\n" +
            "]";

    private static final String AQL_QUERY_NOTE_VISIBLE_FROM_EYE = "LET visible = (FOR entity, rel IN 1..3 OUTBOUND @eye GRAPH 'graph'\n" +
            "  FILTER (rel.kind == 'item' OR rel.kind == 'ref')\n" +
            "  RETURN entity._id\n" +
            ")\n" +
            "\n" +
            "RETURN @note IN visible";

    private static final String AQL_QUERY_NOTE_VISIBLE_TO_PERSON = "LET visible = (\n" +
            "    FOR entity, rel IN 1..10 INBOUND @note GRAPH 'graph'\n" +
            "        FOR person, rel2 IN OUTBOUND entity GRAPH 'graph'\n" +
            "            FILTER (rel.kind == 'item' OR rel.kind == 'ref' OR (rel.kind == 'person' AND person._id == @person))\n" +
            "            FILTER rel2.kind == 'person' AND person._id == @person\n" +
            "            RETURN true\n" +
            ")\n" +
            "\n" +
            "RETURN visible[0] == true";

    private static final String AQL_UPSERT_ENSURE_NOTE_EXISTS = "UPSERT { _key: @note } INSERT { _key: @note, kind: 'note', updated: DATE_NOW(), version: 1 } UPDATE { updated: DATE_NOW(), version: OLD.version + 1 } IN entities";

    private static final String AQL_UPSERT_PROP = "UPSERT { note: @note, type: @prop, kind: 'prop' } INSERT { kind: 'prop', note: @note, updated: DATE_NOW(), version: 1, type: @prop, value: @value } UPDATE { updated: DATE_NOW(), version: OLD.version + 1, value: @value } IN entities";

    private static final String AQL_UPSERT_CLIENT_STATE = "FOR p IN entities FILTER p._id == @prop\n" +
            "    UPSERT { _from: @client, _to: @prop }\n" +
            "        INSERT { _from: @client, _to: @prop, updated: DATE_NOW(), version: p.version }\n" +
            "        UPDATE { updated: DATE_NOW(), version: p.version }\n" +
            "        IN sync";

    private static final String AQL_UPSERT_CLIENT_STATE_BY_NOTE_AND_TYPE = "FOR p IN entities FILTER p.@note AND p.@prop\n" +
            "    UPSERT { _from: @client, _to: p._id }\n" +
            "        INSERT { _from: @client, _to: p._id, updated: DATE_NOW(), version: p.version }\n" +
            "        UPDATE { updated: DATE_NOW(), version: p.version }\n" +
            "        IN sync";

    private static final String AQL_UPSERT_PERSON = "UPSERT { kind: 'person', vlllageId: @person }\n" +
            "    INSERT { kind: 'person', vlllageId: @person }\n" +
            "    UPDATE {}\n" +
            "    IN entities\n" +
            "    RETURN NEW";

    private static final String AQL_UPSERT_CLIENT = "UPSERT { kind: 'client', person: @person, token: @token }\n" +
            "    INSERT { kind: 'client', person: @person, token: @token }\n" +
            "    UPDATE {}\n" +
            "    IN entities\n" +
            "    RETURN NEW";

    private static final String AQL_UPDATE_RELATIONSHIPS_REMOVE_STEP = "FOR relationship IN relationships FILTER relationship._from == @note\n" +
            "        AND relationship.kind == @prop\n" +
            "        AND relationship._id NOT IN @value\n" +
            "    REMOVE relationship IN relationships";

    private static final String AQL_UPDATE_RELATIONSHIPS_INSERT_STEP = "FOR target IN @value\n" +
            "    UPSERT { _from: @note, _to: target, kind: @prop }\n" +
            "        INSERT { _from: @note, _to: target, kind: @prop }\n" +
            "        UPDATE {}\n" +
            "        IN relationships";

    public List<PropSet> changesUnderNoteForClientToken(String clientId, String noteId, String personId) {
        Map<String, Object> params = new HashMap<>();
        params.put(AQL_PARAM_CLIENT, clientId);
        params.put(AQL_PARAM_NOTE, noteId);
//        params.put(AQL_PARAM_PERSON, personId);

        try (ArangoCursor<String> arangoCursor = Arango.getDb().query(AQL_QUERY_CHANGES_FOR_CLIENT_VISIBLE_FROM_NOTE, params, AQL_QUERY_OPTIONS, String.class)) {
            return arangoCursor.asListRemaining()
                    .stream().map(str -> {
                        JsonArray note = Json.json.fromJson(str, JsonArray.class);

                        List<NoteProp> props = new ArrayList<>();
                        note.get(1).getAsJsonArray().forEach(jsonElement -> {
                            props.add(new NoteProp(
                                    jsonElement.getAsJsonArray().get(0).getAsString(),
                                    jsonElement.getAsJsonArray().get(1).getAsString(),
                                    jsonElement.getAsJsonArray().get(2)));
                        });

                        return new PropSet(note.get(0).getAsString(), props);
                    }).collect(Collectors.toList());
        } catch (IOException | ArangoDBException e) {
            print(params);
            throw new RuntimeException(e);
        }
    }

    public boolean noteVisibleFromEye(String eyeKey, String noteKey) {
        Map<String, Object> params = new HashMap<>();
        params.put(AQL_PARAM_EYE, Arango.id(eyeKey));
        params.put(AQL_PARAM_NOTE, Arango.id(noteKey));
        try (ArangoCursor<Boolean> arangoCursor = Arango.getDb().query(AQL_QUERY_NOTE_VISIBLE_FROM_EYE, params, AQL_QUERY_OPTIONS, Boolean.class)) {
            return arangoCursor.hasNext() && arangoCursor.next();
        } catch (IOException | ArangoDBException e) {
            print(params);
            throw new RuntimeException(e);
        }
    }

    public boolean noteVisibleToPerson(String noteId, String personId) {
        Map<String, Object> params = new HashMap<>();
        params.put(AQL_PARAM_NOTE, noteId);
        params.put(AQL_PARAM_PERSON, personId);
        try (ArangoCursor<Boolean> arangoCursor = Arango.getDb().query(AQL_QUERY_NOTE_VISIBLE_TO_PERSON, params, AQL_QUERY_OPTIONS, Boolean.class)) {
            return arangoCursor.hasNext() && arangoCursor.next();
        } catch (IOException | ArangoDBException e) {
            print(params);
            throw new RuntimeException(e);
        }
    }

    public void saveNote(String key) {
        Map<String, Object> params = new HashMap<>();
        params.put(AQL_PARAM_NOTE, key);

        try (ArangoCursor<Void> ignored = Arango.getDb().query(AQL_UPSERT_ENSURE_NOTE_EXISTS, params, AQL_QUERY_OPTIONS, Void.class)) {
            // Do nothing
        } catch (IOException | ArangoDBException e) {
            print(params);
            throw new RuntimeException(e);
        }
    }

    public void saveNoteProp(String noteKey, String propType, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(AQL_PARAM_NOTE, Arango.id(noteKey));
        params.put(AQL_PARAM_PROP, propType);
        params.put(AQL_PARAM_VALUE, value);

        try (ArangoCursor<Void> ignored = Arango.getDb().query(AQL_UPSERT_PROP, params, AQL_QUERY_OPTIONS, Void.class)) {
            // Do nothing
        } catch (IOException | ArangoDBException e) {
            print(params);
            throw new RuntimeException(e);
        }
    }

    public void setPropSeenByClient(String clientId, String propId) {
        Map<String, Object> params = new HashMap<>();
        params.put(AQL_PARAM_PROP, propId);
        params.put(AQL_PARAM_CLIENT, clientId);

        try (ArangoCursor<Void> ignored = Arango.getDb().query(AQL_UPSERT_CLIENT_STATE, params, AQL_QUERY_OPTIONS, Void.class)) {
            // Do nothing
        } catch (IOException | ArangoDBException e) {
            print(params);
            throw new RuntimeException(e);
        }
    }

    public void setPropSeenByClient(String clientId, String noteKey, String propType) {
        Map<String, Object> params = new HashMap<>();
        params.put(AQL_PARAM_NOTE, Arango.id(noteKey));
        params.put(AQL_PARAM_PROP, propType);
        params.put(AQL_PARAM_CLIENT, clientId);

        try (ArangoCursor<Void> ignored = Arango.getDb().query(AQL_UPSERT_CLIENT_STATE_BY_NOTE_AND_TYPE, params, AQL_QUERY_OPTIONS, Void.class)) {
            // Do nothing
        } catch (IOException | ArangoDBException e) {
            print(params);
            throw new RuntimeException(e);
        }
    }

    public BaseDocument getPerson(String vlllageId) {
        Map<String, Object> params = new HashMap<>();
        params.put(AQL_PARAM_PERSON, vlllageId);

        try (ArangoCursor<BaseDocument> arangoCursor = Arango.getDb().query(AQL_UPSERT_PERSON, params, AQL_QUERY_OPTIONS, BaseDocument.class)) {
            return arangoCursor.next();
        } catch (IOException | ArangoDBException e) {
            print(params);
            throw new RuntimeException(e);
        }
    }

    public BaseDocument getClient(String personId, String token) {
        Map<String, Object> params = new HashMap<>();
        params.put(AQL_PARAM_PERSON, personId);
        params.put(AQL_PARAM_TOKEN, token);

        try (ArangoCursor<BaseDocument> arangoCursor = Arango.getDb().query(AQL_UPSERT_CLIENT, params, AQL_QUERY_OPTIONS, BaseDocument.class)) {
            return arangoCursor.next();
        } catch (IOException | ArangoDBException e) {
            print(params);
            throw new RuntimeException(e);
        }
    }

    public void updateRelationshipsForNoteProp(String noteKey, String prop, List<String> relationshipKeys) {
        Map<String, Object> params = new HashMap<>();
        params.put(AQL_PARAM_NOTE, Arango.id(noteKey));
        params.put(AQL_PARAM_PROP, prop);
        params.put(AQL_PARAM_VALUE, relationshipKeys.stream()
                .map(Arango::id).collect(Collectors.toList()));

        try (ArangoCursor<Void> ignored = Arango.getDb().query(AQL_UPDATE_RELATIONSHIPS_REMOVE_STEP, params, AQL_QUERY_OPTIONS, Void.class)) {
            // Do nothing
        } catch (IOException | ArangoDBException e) {
            print(params);
            throw new RuntimeException(e);
        }

        try (ArangoCursor<Void> ignored = Arango.getDb().query(AQL_UPDATE_RELATIONSHIPS_INSERT_STEP, params, AQL_QUERY_OPTIONS, Void.class)) {
            // Do nothing
        } catch (IOException | ArangoDBException e) {
            print(params);
            throw new RuntimeException(e);
        }
    }

    private void print(Map<String, Object> map) {
        Logger.getAnonymousLogger().warning("map = " + String.valueOf(map));
        map.forEach((key, value) -> {
            Logger.getAnonymousLogger().warning(key + " = " + String.valueOf(value));
        });
    }

    public static String relToProp(String type) {
        switch (type) {
            case "item": return "items";
            case "ref": return "ref";
            case "person": return "people";
            default: throw new RuntimeException("Unknown relationship type: " + type);
        }
    }
}