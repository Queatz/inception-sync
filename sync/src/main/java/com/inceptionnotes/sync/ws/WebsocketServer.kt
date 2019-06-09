package com.inceptionnotes.sync.ws

import com.inceptionnotes.sync.events.Event
import com.inceptionnotes.sync.world.Server
import java.util.*
import javax.websocket.HandshakeResponse
import javax.websocket.server.HandshakeRequest
import javax.websocket.server.ServerEndpointConfig

/**
 * Created by jacob on 1/20/18.
 */

class WebsocketServer : ServerEndpointConfig.Configurator() {

    private val server: Server = Server(this)
    private val sessions = HashSet<WebsocketClient>()

    fun broadcast(client: WebsocketClient, event: Event) {
        synchronized(sessions) {
            for (other in sessions) {
                if (!other.session.isOpen) {
                    continue
                }

                if (client.session.id == other.session.id) {
                    continue
                }

                other.client.send(event)
            }
        }
    }

    fun join(client: WebsocketClient) {
        synchronized(sessions) {
            sessions.add(client)
        }
    }

    fun leave(client: WebsocketClient) {
        synchronized(sessions) {
            sessions.remove(client)
        }
    }

    override fun modifyHandshake(conf: ServerEndpointConfig, req: HandshakeRequest, resp: HandshakeResponse) {
        conf.userProperties["server"] = server
    }
}
