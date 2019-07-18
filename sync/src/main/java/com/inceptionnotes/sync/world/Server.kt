package com.inceptionnotes.sync.world

import com.queatz.on.On

/**
 * Created by jacob on 1/20/18.
 */

class Server(internal val on: On) {
    fun join(client: Client) = on<World>().join(client)
    fun leave(client: Client) = on<World>().leave(client)
}
