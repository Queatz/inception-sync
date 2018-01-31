package com.inceptionnotes.sync.events;

import com.inceptionnotes.sync.Client;

/**
 * Created by jacob on 1/30/18.
 */

public class SimpleMessageServerEvent extends Event {

    public String message;

    public SimpleMessageServerEvent() {
    }

    public SimpleMessageServerEvent(String message) {
        this.message = message;
    }

    @Override
    public void got(Client client) {
        // Do nothing
    }
}
