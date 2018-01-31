package com.inceptionnotes.sync.events;

import com.inceptionnotes.sync.Client;
import com.inceptionnotes.sync.objects.Note;

import java.util.List;

/**
 * Created by jacob on 1/26/18.
 */

public class SyncEvent extends Event {

    public List<Note> notes;

    @Override
    public void got(Client client) {
        if (client.getClientToken() == null || client.getPersonToken() == null) {
            client.send(new SimpleMessageServerEvent("hey matey. plz identify urself matey. thx matey."));
        }
    }
}
