package com.inceptionnotes.sync.util

import com.inceptionnotes.sync.events.*
import java.util.*

/**
 * Created by jacob on 1/26/18.
 */

object Events {
    val events: MutableMap<String, Class<out Event>> = HashMap()
    val actions: MutableMap<Class<out Event>, String> = HashMap()

    init {
        events["sync"] = SyncEvent::class.java
        events["identify"] = IdentifyEvent::class.java
        events["message"] = SimpleMessageServerEvent::class.java
        events["show"] = ShowEvent::class.java
        events["server"] = ServerEvent::class.java

        events.forEach { (k, v) -> actions[v] = k }
    }
}
