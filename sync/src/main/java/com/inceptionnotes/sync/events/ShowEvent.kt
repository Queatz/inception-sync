package com.inceptionnotes.sync.events

import com.inceptionnotes.sync.world.Client

/**
 * Created by jacob on 2/10/18.
 */

class ShowEvent : Event {

    var show: String? = null

    override fun got(client: Client) {
        if (!client.isIdentified) {
            client.send(SimpleMessageServerEvent("hey matey. plz identify urself matey. thx matey."))
            return
        }

        client.show = show
    }
}
