package com.inceptionnotes.sync.store;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.Protocol;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.HashIndexOptions;

import java.util.HashSet;
import java.util.Set;

public class Arango {

    private static final String DB_USER = "notes";
    private static final String DB_PASS = "notes";
    private static final String DB_DATABASE = "notes";

    public static final String DB_COLLECTION_ENTITIES = "entities";
    public static final String DB_COLLECTION_RELATIONSHIPS = "relationships";
    public static final String DB_COLLECTION_SYNC = "sync";
    public static final String DB_GRAPH = "graph";
    public static final String DB_CLIENT_STATE_GRAPH = "state";

    private static ArangoDatabase __arangoDatabase;

    public static ArangoCollection getEntities() {
        return getDb().collection(DB_COLLECTION_ENTITIES);
    }

    public static ArangoCollection getSync() {
        return getDb().collection(DB_COLLECTION_SYNC);
    }

    public static ArangoCollection getRelationships() {
        return getDb().collection(DB_COLLECTION_RELATIONSHIPS);
    }

    public static ArangoDatabase getDb() {
        if (__arangoDatabase == null) {
            __arangoDatabase = new ArangoDB.Builder()
                    .user(DB_USER)
                    .password(DB_PASS)
                    .useProtocol(Protocol.HTTP_VPACK)
                    .build()
                    .db(DB_DATABASE);

            try {
                __arangoDatabase.createCollection(DB_COLLECTION_ENTITIES);
            } catch (ArangoDBException ignored) {
                // Whatever
            }

            try {
                Set<EdgeDefinition> edges = new HashSet<>();
                edges.add(new EdgeDefinition()
                        .collection(DB_COLLECTION_RELATIONSHIPS)
                        .from(DB_COLLECTION_ENTITIES)
                        .to(DB_COLLECTION_ENTITIES));

                __arangoDatabase.createCollection(DB_COLLECTION_RELATIONSHIPS, new CollectionCreateOptions().type(CollectionType.EDGES));
                __arangoDatabase.createGraph(DB_GRAPH, edges);
            } catch (ArangoDBException ignored) {
                // Whatever
            }

            try {
                Set<EdgeDefinition> edges = new HashSet<>();
                edges.add(new EdgeDefinition()
                        .collection(DB_COLLECTION_SYNC)
                        .from(DB_COLLECTION_ENTITIES)
                        .to(DB_COLLECTION_ENTITIES));

                __arangoDatabase.createCollection(DB_COLLECTION_SYNC, new CollectionCreateOptions().type(CollectionType.EDGES));
                __arangoDatabase.createGraph(DB_CLIENT_STATE_GRAPH, edges);
            } catch (ArangoDBException ignored) {
                // Whatever
            }

            Set<String> noteIndex = new HashSet<>();
            noteIndex.add("note");
            __arangoDatabase.collection(DB_COLLECTION_ENTITIES).ensureHashIndex(noteIndex, new HashIndexOptions());

            Set<String> vlllageIdIndex = new HashSet<>();
            vlllageIdIndex.add("vlllageId");
            __arangoDatabase.collection(DB_COLLECTION_ENTITIES).ensureHashIndex(vlllageIdIndex, new HashIndexOptions());

            Set<String> tokenIndex = new HashSet<>();
            tokenIndex.add("token");
            __arangoDatabase.collection(DB_COLLECTION_ENTITIES).ensureHashIndex(tokenIndex, new HashIndexOptions());

            Set<String> typeIndex = new HashSet<>();
            typeIndex.add("type");
            __arangoDatabase.collection(DB_COLLECTION_ENTITIES).ensureHashIndex(typeIndex, new HashIndexOptions());

            Set<String> kindIndex = new HashSet<>();
            kindIndex.add("kind");
            __arangoDatabase.collection(DB_COLLECTION_ENTITIES).ensureHashIndex(kindIndex, new HashIndexOptions());
            __arangoDatabase.collection(DB_COLLECTION_RELATIONSHIPS).ensureHashIndex(kindIndex, new HashIndexOptions());

            Set<String> versionIndex = new HashSet<>();
            kindIndex.add("version");
            __arangoDatabase.collection(DB_COLLECTION_ENTITIES).ensureHashIndex(versionIndex, new HashIndexOptions());

            Set<String> Index = new HashSet<>();
            kindIndex.add("version");
            __arangoDatabase.collection(DB_COLLECTION_ENTITIES).ensureHashIndex(versionIndex, new HashIndexOptions());
        }

        return __arangoDatabase;
    }

    public static String id(String key) {
        return DB_COLLECTION_ENTITIES + "/" + key;
    }
}
