package com.inceptionnotes.sync.events

import com.inceptionnotes.sync.world.Client

class ServerEvent : Event {

    var name: String? = null

    constructor()

    constructor(name: String) {
        this.name = name
    }

    override fun got(client: Client) {
        // Do nothing
    }
}
