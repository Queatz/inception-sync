package com.inceptionnotes.sync.store;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.model.CollectionCreateOptions;

import java.util.HashSet;
import java.util.Set;

public class Store {

    private static final String DB_USER = "notes";
    private static final String DB_PASS = "notes";
    private static final String DB_DATABASE = "notes";

    public static final String DB_COLLECTION = "notes";
    public static final String DB_RELATIONSHIPS = "relationships";
    public static final String DB_GRAPH = "graph";

    private static ArangoDatabase __arangoDatabase;

    public static ArangoCollection getCollection() {
        return getDb().collection(DB_COLLECTION);
    }

    public static ArangoCollection getRelationships() {
        return getDb().collection(DB_RELATIONSHIPS);
    }

    public static ArangoDatabase getDb() {
        if (__arangoDatabase == null) {
            __arangoDatabase = new ArangoDB.Builder()
                    .user(DB_USER)
                    .password(DB_PASS)
                    .build()
                    .db(DB_DATABASE);

            try {
                __arangoDatabase.createCollection(DB_COLLECTION);
            } catch (ArangoDBException ignored) {
                // Whatever
            }

            try {
                Set<EdgeDefinition> edges = new HashSet<>();
                edges.add(new EdgeDefinition()
                        .collection(DB_RELATIONSHIPS)
                        .from(DB_COLLECTION)
                        .to(DB_COLLECTION));

                __arangoDatabase.createCollection(DB_RELATIONSHIPS, new CollectionCreateOptions().type(CollectionType.EDGES));
                __arangoDatabase.createGraph(DB_GRAPH, edges);
            } catch (ArangoDBException ignored) {
                // Whatever
            }
        }

        return __arangoDatabase;
    }
}
