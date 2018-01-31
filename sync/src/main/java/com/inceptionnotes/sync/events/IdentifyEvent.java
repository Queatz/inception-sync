package com.inceptionnotes.sync.events;

import com.inceptionnotes.sync.Client;

/**
 * Created by jacob on 1/30/18.
 */

public class IdentifyEvent extends Event {

    public String me;
    public String client;

    @Override
    public void got(Client client) {
        client.identify(me, this.client);
    }
}
