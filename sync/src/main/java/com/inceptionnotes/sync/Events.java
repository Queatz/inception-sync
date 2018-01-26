package com.inceptionnotes.sync;

import com.inceptionnotes.sync.events.Event;
import com.inceptionnotes.sync.events.SyncEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jacob on 1/26/18.
 */

public class Events {
    public static final Map<String, Class<? extends Event>> events = new HashMap<>();
    public static final Map<Class<? extends Event>, String> actions = new HashMap<>();

    static {
        events.put("sync", SyncEvent.class);

        events.forEach((k, v) -> actions.put(v, k));
    }
}
