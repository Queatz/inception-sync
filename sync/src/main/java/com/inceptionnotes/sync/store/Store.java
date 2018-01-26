package com.inceptionnotes.sync.store;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;

public class Store {

    private static final String DB_USER = "notes";
    private static final String DB_PASS = "notes";
    private static final String DB_DATABASE = "notes";
    public static final String DB_COLLECTION = "notes";

    private static ArangoDatabase __arangoDatabase;

    public static ArangoCollection getCollection() {
        return getDb().collection(DB_COLLECTION);
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
        }

        return __arangoDatabase;
    }
}
