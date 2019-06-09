package com.inceptionnotes.sync.store

import com.arangodb.*
import com.arangodb.entity.CollectionType
import com.arangodb.entity.EdgeDefinition
import com.arangodb.model.CollectionCreateOptions
import com.arangodb.model.HashIndexOptions
import java.util.*

object Arango {

    private const val DB_USER = "notes"
    private const val DB_PASS = "notes"
    private const val DB_DATABASE = "notes"

    private const val DB_COLLECTION_ENTITIES = "entities"
    private const val DB_COLLECTION_RELATIONSHIPS = "relationships"
    private const val DB_COLLECTION_SYNC = "sync"
    private const val DB_GRAPH = "graph"
    private const val DB_CLIENT_STATE_GRAPH = "state"

    private lateinit var arangoDatabase: ArangoDatabase

    val entities: ArangoCollection
        get() = db.collection(DB_COLLECTION_ENTITIES)

    val sync: ArangoCollection
        get() = db.collection(DB_COLLECTION_SYNC)

    val relationships: ArangoCollection
        get() = db.collection(DB_COLLECTION_RELATIONSHIPS)

    val db: ArangoDatabase
        get() {
            if (!this::arangoDatabase.isInitialized) {
                arangoDatabase = ArangoDB.Builder()
                        .user(DB_USER)
                        .password(DB_PASS)
                        .useProtocol(Protocol.HTTP_VPACK)
                        .build()
                        .db(DB_DATABASE)

                try {
                    arangoDatabase.createCollection(DB_COLLECTION_ENTITIES)
                } catch (ignored: ArangoDBException) {
                }

                try {
                    val edges = HashSet<EdgeDefinition>()
                    edges.add(EdgeDefinition()
                            .collection(DB_COLLECTION_RELATIONSHIPS)
                            .from(DB_COLLECTION_ENTITIES)
                            .to(DB_COLLECTION_ENTITIES))

                    arangoDatabase.createCollection(DB_COLLECTION_RELATIONSHIPS, CollectionCreateOptions().type(CollectionType.EDGES))
                    arangoDatabase.createGraph(DB_GRAPH, edges)
                } catch (ignored: ArangoDBException) {
                }

                try {
                    val edges = HashSet<EdgeDefinition>()
                    edges.add(EdgeDefinition()
                            .collection(DB_COLLECTION_SYNC)
                            .from(DB_COLLECTION_ENTITIES)
                            .to(DB_COLLECTION_ENTITIES))

                    arangoDatabase.createCollection(DB_COLLECTION_SYNC, CollectionCreateOptions().type(CollectionType.EDGES))
                    arangoDatabase.createGraph(DB_CLIENT_STATE_GRAPH, edges)
                } catch (ignored: ArangoDBException) {
                }

                val noteIndex = HashSet<String>()
                noteIndex.add("note")
                arangoDatabase.collection(DB_COLLECTION_ENTITIES).ensureHashIndex(noteIndex, HashIndexOptions())

                val vlllageIdIndex = HashSet<String>()
                vlllageIdIndex.add("vlllageId")
                arangoDatabase.collection(DB_COLLECTION_ENTITIES).ensureHashIndex(vlllageIdIndex, HashIndexOptions())

                val tokenIndex = HashSet<String>()
                tokenIndex.add("token")
                arangoDatabase.collection(DB_COLLECTION_ENTITIES).ensureHashIndex(tokenIndex, HashIndexOptions())

                val typeIndex = HashSet<String>()
                typeIndex.add("type")
                arangoDatabase.collection(DB_COLLECTION_ENTITIES).ensureHashIndex(typeIndex, HashIndexOptions())

                val kindIndex = HashSet<String>()
                kindIndex.add("kind")
                arangoDatabase.collection(DB_COLLECTION_ENTITIES).ensureHashIndex(kindIndex, HashIndexOptions())
                arangoDatabase.collection(DB_COLLECTION_RELATIONSHIPS).ensureHashIndex(kindIndex, HashIndexOptions())

                val versionIndex = HashSet<String>()
                kindIndex.add("version")
                arangoDatabase.collection(DB_COLLECTION_ENTITIES).ensureHashIndex(versionIndex, HashIndexOptions())
            }

            return arangoDatabase
        }

    fun id(key: String) = "$DB_COLLECTION_ENTITIES/$key"
}
