package com.inceptionnotes.sync.ws


import com.inceptionnotes.sync.world.Client
import com.inceptionnotes.sync.world.Server
import java.io.IOException
import java.util.concurrent.TimeUnit.MINUTES
import java.util.logging.Logger
import javax.websocket.*
import javax.websocket.server.ServerEndpoint

@ServerEndpoint(value = "/ws", configurator = WebsocketServer::class)
class WebsocketClient {

    lateinit var session: Session
        private set
    lateinit var server: Server
        private set
    lateinit var client: Client
        private set

    @OnOpen
    fun onOpen(session: Session, endpointConfig: EndpointConfig) {
        Logger.getAnonymousLogger().info("WEBSOCKET (SESSION): " + session.id)
        this.session = session

        session.maxIdleTimeout = MINUTES.toMillis(30)
        server = endpointConfig.userProperties["server"] as Server
        client = Client(this)
        server.join(client)
    }

    @OnClose
    fun onClose() {
        if (session.isOpen) {
            try {
                session.close()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        }

        Logger.getAnonymousLogger().info("WEBSOCKET: END")
        server.leave(client)
        client.close()
    }

    @OnMessage
    @Throws(IOException::class)
    fun onMessage(message: String) {
        Logger.getAnonymousLogger().info("WEBSOCKET (MESSAGE): " + if (message.length > 128) message.substring(0, 127) + "..." + message.length else message)
        client.got(message)
    }

    @OnMessage
    @Throws(IOException::class)
    fun onData(data: ByteArray) {
        Logger.getAnonymousLogger().info("WEBSOCKET (DATA): " + data.size)
        client.got(data)
    }

    @OnError
    @Throws(Throwable::class)
    fun onError(t: Throwable) {
        Logger.getAnonymousLogger().info("WEBSOCKET (ERROR): " + t.message)
        t.printStackTrace()
    }
}