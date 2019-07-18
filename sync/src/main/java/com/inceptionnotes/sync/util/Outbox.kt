package com.inceptionnotes.sync.util

import com.google.gson.JsonArray
import com.inceptionnotes.sync.events.Event

class Outbox {

    private val outbox = mutableListOf<Event>()

    fun add(event: Event) {
        outbox.add(event)
    }


    fun clear() {
        outbox.clear()
    }


    fun json(): String {
        val outboxEvents = JsonArray()

        outbox.forEach {
            val event = JsonArray()
            event.add(Events.actions[it.javaClass])
            event.add(Json.json.toJsonTree(it))
            outboxEvents.add(event)
        }

        return Json.json.toJson(outboxEvents)
    }
}