package com.inceptionnotes.sync.store

import com.arangodb.ArangoDBException
import com.arangodb.entity.BaseDocument
import com.arangodb.model.AqlQueryOptions
import com.google.gson.JsonArray
import com.inceptionnotes.sync.Json
import java.io.IOException
import java.util.*
import java.util.logging.Logger
import java.util.stream.Collectors

/**
 * Created by jacob on 2/1/18.
 */

class NoteStore {

    fun changesUnderNoteForClientToken(clientId: String, noteId: String, personId: String?): List<PropSet> {
        val params = HashMap<String, Any?>()
        params[AQL_PARAM_CLIENT] = clientId
        params[AQL_PARAM_NOTE] = noteId
        //        params.put(AQL_PARAM_PERSON, personId);

        try {
            Arango.db.query(AQL_QUERY_CHANGES_FOR_CLIENT_VISIBLE_FROM_NOTE, params, AQL_QUERY_OPTIONS, String::class.java).use { arangoCursor ->
                return arangoCursor.asListRemaining()
                        .stream().map { str ->
                            val note = Json.json.fromJson<JsonArray>(str, JsonArray::class.java)

                            val props = ArrayList<NoteProp>()
                            note.get(1).asJsonArray.forEach { jsonElement ->
                                props.add(NoteProp(
                                        jsonElement.asJsonArray.get(0).asString,
                                        jsonElement.asJsonArray.get(1).asString,
                                        jsonElement.asJsonArray.get(2)))
                            }

                            PropSet(note.get(0).asString, props)
                        }.collect(Collectors.toList<PropSet>())
            }
        } catch (e: IOException) {
            print(params)
            throw RuntimeException(e)
        } catch (e: ArangoDBException) {
            print(params)
            throw RuntimeException(e)
        }

    }

    fun noteVisibleFromEye(eyeKey: String, noteKey: String): Boolean {
        val params = HashMap<String, Any?>()
        params[AQL_PARAM_EYE] = Arango.id(eyeKey)
        params[AQL_PARAM_NOTE] = Arango.id(noteKey)
        try {
            Arango.db.query(AQL_QUERY_NOTE_VISIBLE_FROM_EYE, params, AQL_QUERY_OPTIONS, Boolean::class.java).use { arangoCursor ->
                return arangoCursor.hasNext() && arangoCursor.next()
            }
        } catch (e: IOException) {
            print(params)
            throw RuntimeException(e)
        } catch (e: ArangoDBException) {
            print(params)
            throw RuntimeException(e)
        }

    }

    fun noteVisibleToPerson(noteId: String, personId: String): Boolean {
        val params = HashMap<String, Any?>()
        params[AQL_PARAM_NOTE] = noteId
        params[AQL_PARAM_PERSON] = personId
        try {
            Arango.db.query(AQL_QUERY_NOTE_VISIBLE_TO_PERSON, params, AQL_QUERY_OPTIONS, Boolean::class.java).use { arangoCursor ->
                return arangoCursor.hasNext() && arangoCursor.next()
            }
        } catch (e: IOException) {
            print(params)
            throw RuntimeException(e)
        } catch (e: ArangoDBException) {
            print(params)
            throw RuntimeException(e)
        }

    }

    fun saveNote(key: String) {
        val params = HashMap<String, Any?>()
        params[AQL_PARAM_NOTE] = key

        try {
            Arango.db.query(AQL_UPSERT_ENSURE_NOTE_EXISTS, params, AQL_QUERY_OPTIONS, Void::class.java).use { }
        } catch (e: IOException) {
            print(params)
            throw RuntimeException(e)
        } catch (e: ArangoDBException) {
            print(params)
            throw RuntimeException(e)
        }

    }

    fun saveNoteProp(noteKey: String, propType: String, value: Any) {
        val params = HashMap<String, Any?>()
        params[AQL_PARAM_NOTE] = Arango.id(noteKey)
        params[AQL_PARAM_PROP] = propType
        params[AQL_PARAM_VALUE] = value

        try {
            Arango.db.query(AQL_UPSERT_PROP, params, AQL_QUERY_OPTIONS, Void::class.java).use { }
        } catch (e: IOException) {
            print(params)
            throw RuntimeException(e)
        } catch (e: ArangoDBException) {
            print(params)
            throw RuntimeException(e)
        }

    }

    fun setPropSeenByClient(clientId: String, propId: String) {
        val params = HashMap<String, Any?>()
        params[AQL_PARAM_PROP] = propId
        params[AQL_PARAM_CLIENT] = clientId

        try {
            Arango.db.query(AQL_UPSERT_CLIENT_STATE, params, AQL_QUERY_OPTIONS, Void::class.java).use { }
        } catch (e: IOException) {
            print(params)
            throw RuntimeException(e)
        } catch (e: ArangoDBException) {
            print(params)
            throw RuntimeException(e)
        }

    }

    fun setPropSeenByClient(clientId: String, noteKey: String, propType: String) {
        val params = HashMap<String, Any?>()
        params[AQL_PARAM_NOTE] = Arango.id(noteKey)
        params[AQL_PARAM_PROP] = propType
        params[AQL_PARAM_CLIENT] = clientId

        try {
            Arango.db.query(AQL_UPSERT_CLIENT_STATE_BY_NOTE_AND_TYPE, params, AQL_QUERY_OPTIONS, Void::class.java).use { }
        } catch (e: IOException) {
            print(params)
            throw RuntimeException(e)
        } catch (e: ArangoDBException) {
            print(params)
            throw RuntimeException(e)
        }

    }

    fun getPerson(vlllageId: String): BaseDocument {
        val params = HashMap<String, Any?>()
        params[AQL_PARAM_PERSON] = vlllageId

        try {
            Arango.db.query(AQL_UPSERT_PERSON, params, AQL_QUERY_OPTIONS, BaseDocument::class.java).use { arangoCursor -> return arangoCursor.next() }
        } catch (e: IOException) {
            print(params)
            throw RuntimeException(e)
        } catch (e: ArangoDBException) {
            print(params)
            throw RuntimeException(e)
        }

    }

    fun getClient(personId: String?, token: String?): BaseDocument {
        val params = HashMap<String, Any?>()
        params[AQL_PARAM_PERSON] = personId
        params[AQL_PARAM_TOKEN] = token

        try {
            Arango.db.query(AQL_UPSERT_CLIENT, params, AQL_QUERY_OPTIONS, BaseDocument::class.java).use { arangoCursor -> return arangoCursor.next() }
        } catch (e: IOException) {
            print(params)
            throw RuntimeException(e)
        } catch (e: ArangoDBException) {
            print(params)
            throw RuntimeException(e)
        }

    }

    fun updateRelationshipsForNoteProp(noteKey: String, prop: String, relationshipKeys: List<String>) {
        val params = HashMap<String, Any?>()
        params[AQL_PARAM_NOTE] = Arango.id(noteKey)
        params[AQL_PARAM_PROP] = prop
        params[AQL_PARAM_VALUE] = relationshipKeys.stream().map { Arango.id(it) }.collect(Collectors.toList())

        try {
            Arango.db.query(AQL_UPDATE_RELATIONSHIPS_REMOVE_STEP, params, AQL_QUERY_OPTIONS, Void::class.java).use {  }
        } catch (e: IOException) {
            print(params)
            throw RuntimeException(e)
        } catch (e: ArangoDBException) {
            print(params)
            throw RuntimeException(e)
        }

        try {
            Arango.db.query(AQL_UPDATE_RELATIONSHIPS_INSERT_STEP, params, AQL_QUERY_OPTIONS, Void::class.java).use {  }
        } catch (e: IOException) {
            print(params)
            throw RuntimeException(e)
        } catch (e: ArangoDBException) {
            print(params)
            throw RuntimeException(e)
        }

    }

    private fun print(map: Map<String, Any>) {
        Logger.getAnonymousLogger().warning("map = $map")
        map.forEach { (key, value) -> Logger.getAnonymousLogger().warning("$key = $value") }
    }

    companion object {

        private val AQL_QUERY_OPTIONS = AqlQueryOptions()

        const val AQL_PARAM_CLIENT = "client"
        const val AQL_PARAM_TOKEN = "token"
        const val AQL_PARAM_NOTE = "note"
        const val AQL_PARAM_EYE = "eye"
        const val AQL_PARAM_PERSON = "person"
        const val AQL_PARAM_PROP = "prop"
        const val AQL_PARAM_VALUE = "value"

        const val AQL_QUERY_CHANGES_FOR_CLIENT_VISIBLE_FROM_NOTE = "LET visible = APPEND(\n" +
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
                "]"

        const val AQL_QUERY_NOTE_VISIBLE_FROM_EYE = "LET visible = (FOR entity, rel IN 1..3 OUTBOUND @eye GRAPH 'graph'\n" +
                "  FILTER (rel.kind == 'item' OR rel.kind == 'ref')\n" +
                "  RETURN entity._id\n" +
                ")\n" +
                "\n" +
                "RETURN @note IN visible"

        const val AQL_QUERY_NOTE_VISIBLE_TO_PERSON = "LET visible = (\n" +
                "    FOR entity, rel IN 1..10 INBOUND @note GRAPH 'graph'\n" +
                "        FOR person, rel2 IN OUTBOUND entity GRAPH 'graph'\n" +
                "            FILTER (rel.kind == 'item' OR rel.kind == 'ref' OR (rel.kind == 'person' AND person._id == @person))\n" +
                "            FILTER rel2.kind == 'person' AND person._id == @person\n" +
                "            RETURN true\n" +
                ")\n" +
                "\n" +
                "RETURN visible[0] == true"

        const val AQL_UPSERT_ENSURE_NOTE_EXISTS = "UPSERT { _key: @note } INSERT { _key: @note, kind: 'note', updated: DATE_NOW(), version: 1 } UPDATE { updated: DATE_NOW(), version: OLD.version + 1 } IN entities"

        const val AQL_UPSERT_PROP = "UPSERT { note: @note, type: @prop, kind: 'prop' } INSERT { kind: 'prop', note: @note, updated: DATE_NOW(), version: 1, type: @prop, value: @value } UPDATE { updated: DATE_NOW(), version: OLD.version + 1, value: @value } IN entities"

        const val AQL_UPSERT_CLIENT_STATE = "FOR p IN entities FILTER p._id == @prop\n" +
                "    UPSERT { _from: @client, _to: @prop }\n" +
                "        INSERT { _from: @client, _to: @prop, updated: DATE_NOW(), version: p.version }\n" +
                "        UPDATE { updated: DATE_NOW(), version: p.version }\n" +
                "        IN sync"

        const val AQL_UPSERT_CLIENT_STATE_BY_NOTE_AND_TYPE = "FOR p IN entities FILTER p.@note AND p.@prop\n" +
                "    UPSERT { _from: @client, _to: p._id }\n" +
                "        INSERT { _from: @client, _to: p._id, updated: DATE_NOW(), version: p.version }\n" +
                "        UPDATE { updated: DATE_NOW(), version: p.version }\n" +
                "        IN sync"

        const val AQL_UPSERT_PERSON = "UPSERT { kind: 'person', vlllageId: @person }\n" +
                "    INSERT { kind: 'person', vlllageId: @person }\n" +
                "    UPDATE {}\n" +
                "    IN entities\n" +
                "    RETURN NEW"

        const val AQL_UPSERT_CLIENT = "UPSERT { kind: 'client', person: @person, token: @token }\n" +
                "    INSERT { kind: 'client', person: @person, token: @token }\n" +
                "    UPDATE {}\n" +
                "    IN entities\n" +
                "    RETURN NEW"

        const val AQL_UPDATE_RELATIONSHIPS_REMOVE_STEP = "FOR relationship IN relationships FILTER relationship._from == @note\n" +
                "        AND relationship.kind == @prop\n" +
                "        AND relationship._id NOT IN @value\n" +
                "    REMOVE relationship IN relationships"

        const val AQL_UPDATE_RELATIONSHIPS_INSERT_STEP = "FOR target IN @value\n" +
                "    UPSERT { _from: @note, _to: target, kind: @prop }\n" +
                "        INSERT { _from: @note, _to: target, kind: @prop }\n" +
                "        UPDATE {}\n" +
                "        IN relationships"

        fun relToProp(type: String) = when (type) {
            "item" -> "items"
            "ref" -> "ref"
            "person" -> "people"
            else -> throw RuntimeException("Unknown relationship type: $type")
        }
    }
}