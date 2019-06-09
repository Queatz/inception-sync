package com.inceptionnotes.sync.world

import com.inceptionnotes.sync.events.Event
import com.inceptionnotes.sync.ws.WebsocketServer

/**
 * Created by jacob on 1/20/18.
 */

class Server(private val websocket: WebsocketServer) {

    val world: World = World()

    fun broadcast(client: Client, event: Event) {
        websocket.broadcast(client.websocket, event)
    }

    fun join(client: Client) {
        websocket.join(client.websocket)
        world.join(client)
    }

    fun leave(client: Client) {
        websocket.leave(client.websocket)
        world.leave(client)
    }
}
