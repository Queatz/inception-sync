package com.inceptionnotes.sync.events

import com.inceptionnotes.sync.world.Client

/**
 * Created by jacob on 1/30/18.
 */

class IdentifyEvent : Event {

    var me: String? = null
    var client: String? = null

    override fun got(client: Client) {
        client.identify(me, this.client)
    }
}
