package com.inceptionnotes.sync.ws


import com.google.gson.JsonArray
import com.inceptionnotes.sync.events.Event
import com.inceptionnotes.sync.events.ServerEvent
import com.inceptionnotes.sync.util.Events
import com.inceptionnotes.sync.util.HttpTransport
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

    private val outbox = mutableListOf<Event>()
    private var clientToken: String? = null

    @OnOpen
    fun onOpen(session: Session, endpointConfig: EndpointConfig) {
        this.session = session

        session.maxIdleTimeout = MINUTES.toMillis(30)
        server = endpointConfig.userProperties["server"] as Server
        client = Client(server.on, server.world, {
            clientToken = it
            server.on<HttpTransport>().register(it, this::onHttpMessage)
        }) {
            outbox.add(it)
            flushOutbox()
        }

        server.join(client)
        client.open()
    }

    private fun onHttpMessage(message: String): String? {
        val events = Json.json.fromJson(message, JsonArray::class.java)

        for (event in events) {
            client.got(Json.json.fromJson(
                    event.asJsonArray.get(1),
                    Events.events[event.asJsonArray.get(0).asString]
            ))
        }

        return outboxToString()
    }

    private fun outboxToString(): String {
        val outboxEvents = JsonArray()

        outbox.forEach {
            val event = JsonArray()
            event.add(Events.actions[it.javaClass])
            event.add(Json.json.toJsonTree(it))
            outboxEvents.add(event)
        }

        return Json.json.toJson(outboxEvents)
    }

    private fun flushOutbox() {
        synchronized(this) {
            try {
                val outboxString = outboxToString()

                if (outboxString.length < 1000) {
                    session.basicRemote.sendText(outboxString)
                } else {
                    val events = JsonArray()
                    val event = JsonArray()
                    val serverEvent = ServerEvent("fetch")
                    event.add(Events.actions[serverEvent::class.java])
                    event.add(Json.json.toJsonTree(serverEvent))
                    events.add(event)
                    session.basicRemote.sendText(Json.json.toJson(events))
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
            } catch (ex: IllegalStateException) {
                ex.printStackTrace()
            }
        }
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

        clientToken?.let { server.on<HttpTransport>().unregister(it) }

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