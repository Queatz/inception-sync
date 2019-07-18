package com.inceptionnotes.sync.util

import com.google.gson.JsonArray
import com.inceptionnotes.sync.store.NoteStore
import com.inceptionnotes.sync.world.Client
import com.queatz.on.On

typealias MessageTrade = (String) -> String?

class HttpTransport constructor(private val on: On) {

    private val clients = mutableMapOf<String, MessageTrade>()

    fun onHttpMessage(token: String, message: String): String? {
        return if (token in clients)
            clients[token]?.invoke(message)
        else
            httpOnly(token, message)
    }

    private fun httpOnly(token: String, message: String): String? {
        val outbox = Outbox()

        val client = Client(on, {}, {
            outbox.add(it)
        })

        client.identify(null, token)
        client.show = on<NoteStore>().getClient(null, token).view

        val events = Json.json.fromJson(message, JsonArray::class.java)

        for (event in events) {
            client.got(Json.json.fromJson(
                    event.asJsonArray.get(1),
                    Events.events[event.asJsonArray.get(0).asString]
            ))
        }

        return outbox.json()
    }

    fun register(token: String, onHttpMessage: MessageTrade) {
        clients[token] = onHttpMessage
    }

    fun unregister(token: String) {
        clients.remove(token)
    }
}