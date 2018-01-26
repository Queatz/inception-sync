package com.inceptionnotes.sync.events;

import com.inceptionnotes.sync.Client;

/**
 * Created by jacob on 1/20/18.
 */

public abstract class Event {
    public abstract void got(Client client);
}
