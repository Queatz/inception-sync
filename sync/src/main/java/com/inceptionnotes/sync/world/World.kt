package com.inceptionnotes.sync.world

import com.inceptionnotes.sync.events.SyncEvent
import com.inceptionnotes.sync.objects.Note
import com.inceptionnotes.sync.store.NoteStore
import com.queatz.on.On
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by jacob on 2/10/18.
 */

class World(private val on: On) {

    private val clients = ConcurrentHashMap.newKeySet<Client>()

    fun join(client: Client) = synchronized(clients) { clients.add(client) }

    fun leave(client: Client) = synchronized(clients) { clients.remove(client) }

    fun onNotesChanged(notes: List<Note>, culprit: Client) = synchronized(clients) {
        clients.forEach { client ->
            if (!client.isIdentified || client.show == null) {
                return@forEach
            }

            if (culprit === client) {
                return@forEach
            }

            val syncEvent = SyncEvent()

            notes.forEach { note ->
                if (note.id == client.show || on<NoteStore>().noteVisibleFromEye(client.show!!, note.id!!)) {
                    syncEvent.notes.add(note)
                }
            }

            client.send(syncEvent)

            syncEvent.notes.forEach { note ->
                note.toSyncNote().sync?.forEach {
                    prop -> on<NoteStore>().setPropSeenByClient(client.clientId!!, note.id!!, prop)
                }
            }
        }
    }
}
