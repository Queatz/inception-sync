package com.inceptionnotes.sync.world

import com.queatz.on.On

/**
 * Created by jacob on 1/20/18.
 */

class Server(internal val on: On) {
    val world = World(on)

    fun join(client: Client) = world.join(client)
    fun leave(client: Client) = world.leave(client)
}
