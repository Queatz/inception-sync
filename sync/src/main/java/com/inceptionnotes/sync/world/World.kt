package com.inceptionnotes.sync.world

import com.inceptionnotes.sync.events.SyncEvent
import com.inceptionnotes.sync.objects.Note
import com.inceptionnotes.sync.store.NoteStore
import com.queatz.on.On
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by jacob on 2/10/18.
 */

class World(private val on: On) {

    private val clients = ConcurrentHashMap.newKeySet<Client>()

    fun join(client: Client) = synchronized(clients) { clients.add(client) }

    fun leave(client: Client) = synchronized(clients) { clients.remove(client) }

    fun onNoteChanged(note: Note, culprit: Client) = synchronized(clients) {
        val syncEvent = SyncEvent().apply {
            notes = ArrayList()
            notes.add(note)
        }

        clients.forEach { client ->
            if (!client.isIdentified || client.show == null) {
                return@forEach
            }

            if (note.id != client.show && !on<NoteStore>().noteVisibleFromEye(client.show!!, note.id!!)) {
                return@forEach
            }

            if (culprit === client) {
                return@forEach
            }

            client.send(syncEvent)

            note.toSyncNote().sync!!.forEach {
                prop -> on<NoteStore>().setPropSeenByClient(client.clientId!!, note.id!!, prop)
            }
        }
    }
}
