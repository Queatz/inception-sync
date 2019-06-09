package com.inceptionnotes.sync

import com.inceptionnotes.sync.objects.Note
import com.inceptionnotes.sync.world.Client
import com.inceptionnotes.sync.world.Server
import com.inceptionnotes.sync.world.World
import com.inceptionnotes.sync.ws.WebsocketClient
import com.inceptionnotes.sync.ws.WebsocketServer
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`

class WorldTest {

    private lateinit var world: World
    private lateinit var server: Server

    @Before
    fun setUp() {
        world = World()

        val websocketServerMock = mock<WebsocketServer>()
        server = Server(websocketServerMock)
    }

    @Test
    fun noteChanged() {
        val client = spy(Client(websocketClientMock()))
        val otherClient = spy(Client(websocketClientMock()))
        world.join(client)
        world.join(otherClient)

        val note = Note()
        world.noteChanged(note, client)

        verify(otherClient, never()).send(any())
        verify(client, never()).send(any())
    }

    private fun websocketClientMock(): WebsocketClient {
        val websocketClient = mock<WebsocketClient>()
        `when`(websocketClient.server).thenReturn(server)
        return websocketClient
    }
}
