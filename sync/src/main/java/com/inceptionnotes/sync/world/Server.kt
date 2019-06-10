package com.inceptionnotes.sync.world

/**
 * Created by jacob on 1/20/18.
 */

class Server {
    val world = World()

    fun join(client: Client) = world.join(client)
    fun leave(client: Client) = world.leave(client)
}
