package com.inceptionnotes.sync.world

import com.inceptionnotes.sync.events.Event
import com.inceptionnotes.sync.events.IdentifyEvent
import com.inceptionnotes.sync.events.ShowEvent
import com.inceptionnotes.sync.events.SyncEvent
import com.inceptionnotes.sync.objects.Note
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class WorldTest {

    private val server = Server()
    private lateinit var client: ClientTest

    @Before
    fun setUp() {
        client = ClientTest(server)
    }

    @Test
    fun clientInitialization() {
        client.identify("client")
        client.verifyNoEvents()

        client.show("somenote")

        client.verifySync {
            it.notes.isEmpty()
        }

        client.sync(Note().apply {
            id = "testnote"
            version = "v1"
            name ="I am note"
        })

        client.verifySync {
            it.notes.size == 1 &&
            it.notes.first().version == "v1" &&
            it.notes.first().sync?.size == 1 &&
            it.notes.first().sync?.first() == "name"
        }
    }

    @Test
    fun twoClients() {

    }
}

class ClientTest constructor(private val server: Server) {
    private val onSendMessage = mock<(Event) -> Unit>()
    private val client = Client(server.world, onSendMessage)

    init {
        val onSendMessage = mock<(Event) -> Unit>()
        val client = Client(server.world, onSendMessage)
        server.join(client)
    }

    fun verifyNoEvents() {
        verify(onSendMessage, never()).invoke(any())
    }

    fun identify(clientToken: String) {
        client.got(IdentifyEvent().also {
            it.client = clientToken
        })
    }

    fun show(show: String) {
        client.got(ShowEvent().also {
            it.show = show
        })
    }

    fun verifySync(block: (SyncEvent) -> Boolean) {
        val captor = argumentCaptor<SyncEvent>()
        verify(onSendMessage, atLeast(1)).invoke(captor.capture())
        Assert.assertTrue(block.invoke(captor.lastValue))
    }

    fun sync(note: Note) {
        client.got(SyncEvent().also {
            it.notes.add(note)
        })
    }
}