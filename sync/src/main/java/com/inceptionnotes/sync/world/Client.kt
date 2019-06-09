package com.inceptionnotes.sync.world

import com.google.gson.JsonArray
import com.inceptionnotes.sync.Events
import com.inceptionnotes.sync.Json
import com.inceptionnotes.sync.events.Event
import com.inceptionnotes.sync.events.SimpleMessageServerEvent
import com.inceptionnotes.sync.events.SyncEvent
import com.inceptionnotes.sync.objects.Note
import com.inceptionnotes.sync.store.Arango
import com.inceptionnotes.sync.store.NoteStore
import com.inceptionnotes.sync.ws.WebsocketClient
import java.io.IOException
import java.util.*
import java.util.logging.Logger

/**
 * Created by jacob on 1/20/18.
 */

class Client(val websocket: WebsocketClient) {
    val noteStore: NoteStore = NoteStore()
    val world: World = websocket.server.world

    // Track client state
    var show: String? = null
        set(show) {
            field = show
            sendUpdatedPropsFromShow()
        }
    var personToken: String? = null
        private set
    var clientToken: String? = null
        private set
    var personId: String? = null
        private set
    var clientId: String? = null
        private set

    val isIdentified: Boolean
        get() = clientToken != null && clientId != null

    fun send(event: Event) {
        val events = JsonArray()
        val e = JsonArray()
        e.add(Events.actions[event.javaClass])
        e.add(Json.json.toJsonTree(event))
        events.add(e)

        synchronized(websocket) {
            try {
                websocket.session.basicRemote.sendText(Json.json.toJson(events))
            } catch (ex: IOException) {
                ex.printStackTrace()
                Logger.getAnonymousLogger().warning("SEND ERROR: " + ex.message)
            } catch (ex: IllegalStateException) {
                ex.printStackTrace()
                Logger.getAnonymousLogger().warning("SEND ERROR: " + ex.message)
            }

        }
    }

    fun got(message: String) {
        val events = Json.json.fromJson<JsonArray>(message, JsonArray::class.java)

        for (event in events) {
            Json.json.fromJson(
                    event.asJsonArray.get(1),
                    Events.events[event.asJsonArray.get(0).asString]
            ).got(this)
        }
    }

    fun got(data: ByteArray) {
        // Do nothing
    }

    fun open() {
        // Do nothing
    }

    fun close() {
        // Do nothing
    }

    fun identify(person: String?, client: String?) {
        personToken = person
        clientToken = client

        Logger.getAnonymousLogger().info("CLIENT IDENTIFIED: client = $clientToken person = $personToken")

        // TODO convert personToken to vlllageId first here
        personToken?.let { personId = noteStore.getPerson(it).id }

        clientToken?.let {
            clientId = noteStore.getClient(personId, it).id
        } ?: run {
            send(SimpleMessageServerEvent("Client id is missing."))
        }
    }

    /**
     * Send updated props to client that the client has not seen yet
     */
    private fun sendUpdatedPropsFromShow() {
        val syncEvent = SyncEvent()
        syncEvent.notes = ArrayList()

        show ?: return

        val clientId = clientId!!
        val show = show!!

        for (propSet in noteStore.changesUnderNoteForClientToken(clientId, Arango.id(show), personId)) {
            val note = Note()
            note.id = propSet.noteId

            propSet.props.forEach { noteProp ->
                note.setProp(noteProp.type, noteProp.value)
                noteStore.setPropSeenByClient(clientId, noteProp.id)
            }

            syncEvent.notes.add(note)
        }

        send(syncEvent)
    }
}