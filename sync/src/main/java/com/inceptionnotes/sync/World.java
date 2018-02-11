package com.inceptionnotes.sync;

import com.inceptionnotes.sync.events.SyncEvent;
import com.inceptionnotes.sync.objects.Note;
import com.inceptionnotes.sync.store.NoteStore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jacob on 2/10/18.
 */

public class World {

    private final NoteStore noteStore = new NoteStore();
    private final Set<Client> clients = new HashSet<>();

    public void join(Client client) {
        clients.add(client);
    }

    public void leave(Client client) {
        clients.remove(client);
    }

    public void noteChanged(Note note) {
        SyncEvent syncEvent = new SyncEvent();
        syncEvent.notes = new ArrayList<>();
        syncEvent.notes.add(note);

        clients.forEach(c -> {
            if (!c.isIdentified() || c.getShow() == null) {
                return;
            }

            if (!noteStore.noteVisibleFromEye(c.getShow(), note.getId())) {
                return;
            }

            c.send(syncEvent);
            note.toSyncNote().getSync().forEach(prop ->
                noteStore.setPropSeenByClient(c.getClientId(), note.getId(), prop));
        });
    }
}