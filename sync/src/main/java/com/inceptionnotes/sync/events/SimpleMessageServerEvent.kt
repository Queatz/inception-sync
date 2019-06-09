package com.inceptionnotes.sync.events

import com.inceptionnotes.sync.world.Client

/**
 * Created by jacob on 1/30/18.
 */

class SimpleMessageServerEvent : Event {

    var message: String? = null

    constructor()

    constructor(message: String) {
        this.message = message
    }

    override fun got(client: Client) {
        // Do nothing
    }
}
