package com.inceptionnotes.sync.events;

import com.inceptionnotes.sync.Client;
import com.inceptionnotes.sync.objects.Note;
import com.inceptionnotes.sync.store.NoteStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jacob on 1/26/18.
 */

public class SyncEvent extends Event {

    public List<Note> notes;

    @Override
    public void got(Client client) {
        if (!client.isIdentified()) {
            client.send(new SimpleMessageServerEvent("hey matey. plz identify urself matey. thx matey."));
            return;
        }

        NoteStore noteStore = client.getNoteStore();

        String personId = noteStore.getPerson(client.getPersonToken()).getId();
        String clientId = noteStore.getClient(personId, client.getClientToken()).getId();

        notes.forEach(n -> {
            noteStore.saveNote(n.getId());
            if (n.getName() != null) {
                noteStore.saveNoteProp(n.getId(), "name", n.getName());
                noteStore.setPropSeenByClient(clientId, "name");
            }

            if (n.getDescription() != null) {
                noteStore.saveNoteProp(n.getId(), "description", n.getDescription());
                noteStore.setPropSeenByClient(clientId, "description");
            }

            if (n.getColor() != null) {
                noteStore.saveNoteProp(n.getId(), "color", n.getColor());
                noteStore.setPropSeenByClient(clientId, "color");
            }

            if (n.getItems() != null) {
                noteStore.saveNoteProp(n.getId(), "items", n.getItems());
                noteStore.setPropSeenByClient(clientId, "items");
            }

            if (n.getRef() != null) {
                noteStore.saveNoteProp(n.getId(), "ref", n.getRef());
                noteStore.setPropSeenByClient(clientId, "ref");
            }

            if (n.getPeople() != null) {
                noteStore.saveNoteProp(n.getId(), "people", n.getPeople());
                noteStore.setPropSeenByClient(clientId, "people");
            }

            if (n.getBackgroundUrl() != null) {
                noteStore.saveNoteProp(n.getId(), "backgroundUrl", n.getBackgroundUrl());
                noteStore.setPropSeenByClient(clientId, "backgroundUrl");
            }

            if (n.getCollapsed() != null) {
                noteStore.saveNoteProp(n.getId(), "collapsed", n.getCollapsed());
                noteStore.setPropSeenByClient(clientId, "collapsed");
            }

            if (n.getEstimate() != null) {
                noteStore.saveNoteProp(n.getId(), "estimate", n.getEstimate());
                noteStore.setPropSeenByClient(clientId, "estimate");
            }
        });

        SyncEvent confirmEvent = new SyncEvent();
        confirmEvent.notes = new ArrayList<>();
        notes.forEach(n -> confirmEvent.notes.add(n.toSyncNote()));
        client.send(confirmEvent);

        notes.forEach(n -> client.getWorld().noteChanged(n));
    }
}
