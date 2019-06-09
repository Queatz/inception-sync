package com.inceptionnotes.sync.world

import com.inceptionnotes.sync.events.SyncEvent
import com.inceptionnotes.sync.objects.Note
import com.inceptionnotes.sync.store.NoteStore
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by jacob on 2/10/18.
 */

class World {

    private val noteStore = NoteStore()
    private val clients = ConcurrentHashMap.newKeySet<Client>()

    fun join(client: Client) {
        clients.add(client)
    }

    fun leave(client: Client) {
        clients.remove(client)
    }

    fun noteChanged(note: Note, culprit: Client) {
        val syncEvent = SyncEvent()
        syncEvent.notes = ArrayList()
        syncEvent.notes.add(note)

        clients.forEach { client ->
            if (!client.isIdentified || client.show == null) {
                return@forEach
            }

            if (note.id != client.show && !noteStore.noteVisibleFromEye(client.show!!, note.id!!)) {
                return@forEach
            }

            if (culprit === client) {
                return@forEach
            }

            client.send(syncEvent)

            note.toSyncNote().sync!!.forEach {
                prop -> noteStore.setPropSeenByClient(client.clientId!!, note.id!!, prop)
            }
        }
    }
}
