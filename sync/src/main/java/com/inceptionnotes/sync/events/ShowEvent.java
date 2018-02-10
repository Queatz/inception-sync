package com.inceptionnotes.sync.events;

import com.inceptionnotes.sync.Client;

/**
 * Created by jacob on 2/10/18.
 */

public class ShowEvent extends Event {

    public String show;

    @Override
    public void got(Client client) {
        if (!client.isIdentified()) {
            client.send(new SimpleMessageServerEvent("hey matey. plz identify urself matey. thx matey."));
            return;
        }

        client.setShow(show);
    }
}
