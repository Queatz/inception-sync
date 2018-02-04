package com.inceptionnotes.sync.store;

import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.AqlQueryOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jacob on 2/1/18.
 */

public class NoteStore {

    private static final String AQL_PARAM_CLIENT = "client";
    private static final String AQL_PARAM_NOTE = "note";
    private static final String AQL_PARAM_EYE = "eye";
    private static final String AQL_PARAM_PERSON = "person";

    private static final String AQL_QUERY_CHANGES_FOR_CLIENT_VISIBLE_FROM_NOTE = "let visible = append(\n" +
            "    (FOR note IN entities FILTER note._id == @note RETURN note),\n" +
            "    (FOR entity, rel IN 1..2 OUTBOUND @note GRAPH \"graph\"\n" +
            "        FILTER (rel.kind == 'item' OR rel.kind == 'ref' OR (rel.kind == 'person' AND entity == @person))\n" +
            "        RETURN entity\n" +
            "    )\n" +
            ")\n" +
            "\n" +
            "for note in visible return {\n" +
            "    id: note._key,\n" +
            "    props: (FOR prop IN entities FILTER prop.note == note._id AND (\n" +
            "        FOR syncProp, sync IN OUTBOUND @client GRAPH \"state\" FILTER syncProp == prop RETURN sync\n" +
            "    )[0].version != prop.version RETURN [\n" +
            "        prop.type,\n" +
            "        prop.value\n" +
            "    ])\n" +
            "}";

    private static final String AQL_QUERY_NOTE_VISIBLE_TO_PERSON_FROM_EYE = "let visible = (FOR entity, rel IN 1..2 OUTBOUND @eye GRAPH \"graph\"\n" +
            "  FILTER (rel.kind == 'item' OR rel.kind == 'ref' OR (rel.kind == 'person' AND entity._id == @person))\n" +
            "  RETURN entity._id\n" +
            ")\n" +
            "\n" +
            "return @note in visible";

    private static final String AQL_QUERY_NOTE_VISIBLE_TO_PERSON = "let visible = (\n" +
            "    FOR entity, rel IN 1..10 INBOUND @note GRAPH 'graph'\n" +
            "        FOR person, rel2 IN OUTBOUND entity GRAPH 'graph'\n" +
            "            FILTER (rel.kind == 'item' OR rel.kind == 'ref' OR (rel.kind == 'person' AND person._id == @person))\n" +
            "            FILTER rel2.kind == 'person' AND person._id == @person\n" +
            "            RETURN true\n" +
            ")\n" +
            "\n" +
            "return visible[0] == true";

    public ArangoCursor<BaseDocument> changesUnderNoteForClientToken(String clientId, String noteId, String personId) {
        Map<String, Object> params = new HashMap<>();
        params.put(AQL_PARAM_CLIENT, clientId);
        params.put(AQL_PARAM_NOTE, noteId);
        params.put(AQL_PARAM_PERSON, personId);
        return Arango.getDb().query(AQL_QUERY_CHANGES_FOR_CLIENT_VISIBLE_FROM_NOTE, params, new AqlQueryOptions(), BaseDocument.class);
    }

    public boolean noteVisibleToPersonFromEye(String eyeId, String noteId, String personId) {
        Map<String, Object> params = new HashMap<>();
        params.put(AQL_PARAM_EYE, eyeId);
        params.put(AQL_PARAM_NOTE, noteId);
        params.put(AQL_PARAM_PERSON, personId);
        ArangoCursor<Boolean> result = Arango.getDb().query(AQL_QUERY_NOTE_VISIBLE_TO_PERSON_FROM_EYE, params, new AqlQueryOptions(), Boolean.class);

        return result.hasNext() && result.next();
    }

    public boolean noteVisibleToPerson(String noteId, String personId) {
        Map<String, Object> params = new HashMap<>();
        params.put(AQL_PARAM_NOTE, noteId);
        params.put(AQL_PARAM_PERSON, personId);
        ArangoCursor<Boolean> result = Arango.getDb().query(AQL_QUERY_NOTE_VISIBLE_TO_PERSON, params, new AqlQueryOptions(), Boolean.class);

        return result.hasNext() && result.next();
    }
}
