package com.inceptionnotes.sync;

import com.inceptionnotes.sync.ws.WebsocketServer;

/**
 * Created by jacob on 1/20/18.
 */

public class Server {

    private WebsocketServer websocket;

    public Server(WebsocketServer websocketServer) {
        this.websocket = websocketServer;
    }

    public void broadcast(Client client, String message) {
        websocket.broadcast(client.getWebsocket(), message);
    }

    public void join(Client client) {
        websocket.join(client.getWebsocket());
    }

    public void leave(Client client) {
        websocket.leave(client.getWebsocket());
    }
}
