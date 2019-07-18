package com.inceptionnotes.sync.events

import com.inceptionnotes.sync.objects.Note
import com.inceptionnotes.sync.store.NoteStore
import com.inceptionnotes.sync.world.Client
import com.inceptionnotes.sync.world.World
import java.util.*

/**
 * Created by jacob on 1/26/18.
 */

class SyncEvent : Event {

    var notes = mutableListOf<Note>()

    override fun got(client: Client) {
        if (!client.isIdentified) {
            client.send(SimpleMessageServerEvent("hey matey. plz identify urself matey. thx matey."))
            return
        }

        val noteStore = client.on<NoteStore>()
        val clientId = client.clientId!!

        val syncNotes = ArrayList<Note>()

        notes.forEach { note ->
            note.id?.let { id ->
                noteStore.saveNote(id)

                if (note.name != null) {
                    noteStore.saveNoteProp(id, "name", note.name!!)
                    noteStore.setPropSeenByClient(clientId, id, "name")
                }

                if (note.description != null) {
                    noteStore.saveNoteProp(id, "description", note.description!!)
                    noteStore.setPropSeenByClient(clientId, id, "description")
                }

                if (note.checked != null) {
                    noteStore.saveNoteProp(id, "checked", note.checked!!)
                    noteStore.setPropSeenByClient(clientId, id, "checked")
                }

                if (note.color != null) {
                    noteStore.saveNoteProp(id, "color", note.color!!)
                    noteStore.setPropSeenByClient(clientId, id, "color")
                }

                if (note.items != null) {
                    noteStore.saveNoteProp(id, "items", note.items!!)
                    noteStore.setPropSeenByClient(clientId, id, "items")
                    noteStore.updateRelationshipsForNoteProp(id, "item", note.items!!)
                }

                if (note.ref != null) {
                    noteStore.saveNoteProp(id, "ref", note.ref!!)
                    noteStore.setPropSeenByClient(clientId, id, "ref")
                    noteStore.updateRelationshipsForNoteProp(id, "ref", note.ref!!)
                }

                if (note.people != null) {
                    noteStore.saveNoteProp(id, "people", note.people!!)
                    noteStore.setPropSeenByClient(clientId, id, "people")
                    noteStore.updateRelationshipsForNoteProp(id, "person", note.people!!)
                }

                if (note.backgroundUrl != null) {
                    noteStore.saveNoteProp(id, "backgroundUrl", note.backgroundUrl!!)
                    noteStore.setPropSeenByClient(clientId, id, "backgroundUrl")
                }

                if (note.collapsed != null) {
                    noteStore.saveNoteProp(id, "collapsed", note.collapsed!!)
                    noteStore.setPropSeenByClient(clientId, id, "collapsed")
                }

                if (note.estimate != null) {
                    noteStore.saveNoteProp(id, "estimate", note.estimate!!)
                    noteStore.setPropSeenByClient(clientId, id, "estimate")
                }
            }

            syncNotes.add(note)
        }

        flush(client, syncNotes)

        client.on<World>().onNotesChanged(notes, client)
    }

    private fun flush(client: Client, notes: List<Note>) {
        val confirmEvent = SyncEvent()
        confirmEvent.notes = ArrayList()
        notes.forEach { n -> confirmEvent.notes.add(n.toSyncNote()) }

        if (confirmEvent.notes.isNotEmpty()) {
            client.send(confirmEvent)
        }
    }
}
