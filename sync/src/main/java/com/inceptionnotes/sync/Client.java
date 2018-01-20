package com.inceptionnotes.sync;

import com.inceptionnotes.sync.ws.WebsocketClient;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by jacob on 1/20/18.
 */

public class Client {

    private WebsocketClient websocket;

    public Client(WebsocketClient websocketClient) {
        this.websocket = websocketClient;
    }

    public void send(String message) {
        try {
            websocket.getSession().getBasicRemote().sendText(message);
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            Logger.getAnonymousLogger().warning("CHAT SEND ERROR: " + e.getMessage());
        }
    }

    public void got(String message) {

    }

    public void got(byte[] data) {
        // Do nothing
    }

    public void open() {

    }

    public void close() {

    }

    public WebsocketClient getWebsocket() {
        return websocket;
    }
}