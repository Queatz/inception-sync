package com.inceptionnotes.sync.world

import com.inceptionnotes.sync.events.Event
import com.inceptionnotes.sync.events.SimpleMessageServerEvent
import com.inceptionnotes.sync.events.SyncEvent
import com.inceptionnotes.sync.objects.Note
import com.inceptionnotes.sync.store.Arango
import com.inceptionnotes.sync.store.NoteStore
import java.util.*

/**
 * Created by jacob on 1/20/18.
 */

class Client constructor(val world: World,
                         private val onSendMessage: (Event) -> Unit) {
    val noteStore = NoteStore()

    // Track client state
    var show: String? = null
        set(value) {
            field = value
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
        onSendMessage(event)
    }

    fun got(event: Event) {
        event.got(this)
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
        show ?: return
        clientId ?: return

        val syncEvent = SyncEvent().apply {
            notes = ArrayList()
        }

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