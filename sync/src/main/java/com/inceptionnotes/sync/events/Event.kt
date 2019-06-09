package com.inceptionnotes.sync.events

import com.inceptionnotes.sync.world.Client

/**
 * Created by jacob on 1/20/18.
 */

interface Event {
    fun got(client: Client)
}
