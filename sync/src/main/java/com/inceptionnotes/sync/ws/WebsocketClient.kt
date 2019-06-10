package com.inceptionnotes.sync.ws


import com.google.gson.JsonArray
import com.inceptionnotes.sync.util.Events
import com.inceptionnotes.sync.util.Json
import com.inceptionnotes.sync.world.Client
import com.inceptionnotes.sync.world.Server
import java.io.IOException
import java.util.concurrent.TimeUnit.MINUTES
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
        this.session = session

        session.maxIdleTimeout = MINUTES.toMillis(30)
        server = endpointConfig.userProperties["server"] as Server
        client = Client(server.world) {
            synchronized(this) {
                try {
                    val events = JsonArray()
                    val event = JsonArray()
                    event.add(Events.actions[it.javaClass])
                    event.add(Json.json.toJsonTree(it))
                    events.add(event)

                    session.basicRemote.sendText(Json.json.toJson(events))
                } catch (ex: IOException) {
                    ex.printStackTrace()
                } catch (ex: IllegalStateException) {
                    ex.printStackTrace()
                }
            }
        }

        server.join(client)
        client.open()
    }

    @OnClose
    fun onClose() {
        if (session.isOpen) try {
            session.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        server.leave(client)
        client.close()
    }

    @OnMessage
    @Throws(IOException::class)
    fun onMessage(message: String) {
        val events = Json.json.fromJson(message, JsonArray::class.java)

        for (event in events) {
            client.got(Json.json.fromJson(
                    event.asJsonArray.get(1),
                    Events.events[event.asJsonArray.get(0).asString]
            ))
        }
    }

    @OnMessage
    @Throws(IOException::class)
    fun onData(data: ByteArray) {
        // Do nothing
    }

    @OnError
    @Throws(Throwable::class)
    fun onError(t: Throwable) {
        t.printStackTrace()
    }
}