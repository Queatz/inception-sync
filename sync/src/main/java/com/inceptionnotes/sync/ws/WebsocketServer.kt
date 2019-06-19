package com.inceptionnotes.sync.ws

import com.inceptionnotes.sync.world.Server
import com.queatz.on.On
import javax.websocket.HandshakeResponse
import javax.websocket.server.HandshakeRequest
import javax.websocket.server.ServerEndpointConfig

/**
 * Created by jacob on 1/20/18.
 */

class WebsocketServer : ServerEndpointConfig.Configurator() {

    companion object {
        internal val on = On()
    }

    private val server = Server(on)

    override fun modifyHandshake(conf: ServerEndpointConfig, req: HandshakeRequest, resp: HandshakeResponse) {
        conf.userProperties["server"] = server
    }
}
