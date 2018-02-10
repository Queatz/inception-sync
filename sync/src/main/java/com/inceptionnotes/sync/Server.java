package com.inceptionnotes.sync;

import com.inceptionnotes.sync.events.Event;
import com.inceptionnotes.sync.ws.WebsocketServer;

/**
 * Created by jacob on 1/20/18.
 */

public class Server {

    private WebsocketServer websocket;
    private World world;

    public Server(WebsocketServer websocketServer) {
        this.websocket = websocketServer;
        this.world = new World();
    }

    public void broadcast(Client client, Event event) {
        websocket.broadcast(client.getWebsocket(), event);
    }

    public void join(Client client) {
        websocket.join(client.getWebsocket());
        world.join(client);
    }

    public void leave(Client client) {
        websocket.leave(client.getWebsocket());
        world.leave(client);
    }

    public World getWorld() {
        return world;
    }
}
