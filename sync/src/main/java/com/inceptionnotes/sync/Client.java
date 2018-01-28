package com.inceptionnotes.sync;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.inceptionnotes.sync.events.Event;
import com.inceptionnotes.sync.ws.WebsocketClient;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by jacob on 1/20/18.
 */

public class Client {

    private WebsocketClient websocket;
    private String currentlyViewing;

    public Client(WebsocketClient websocketClient) {
        this.websocket = websocketClient;
    }

    public void send(Event event) {
        JsonArray events = new JsonArray();
        JsonArray e = new JsonArray();
        e.add(Events.actions.get(event.getClass()));
        e.add(Json.json.toJsonTree(event));
        events.add(e);

        try {
            websocket.getSession().getBasicRemote().sendText(Json.json.toJson(events));
        } catch (IOException | IllegalStateException ex) {
            ex.printStackTrace();
            Logger.getAnonymousLogger().warning("SEND ERROR: " + ex.getMessage());
        }
    }

    public void got(String message) {
        JsonArray events = Json.json.fromJson(message, JsonArray.class);

        for (JsonElement event : events) {
            Json.json.fromJson(
                    event.getAsJsonArray().get(1),
                    Events.events.get(event.getAsJsonArray().get(0).getAsString())
            ).got(this);
        }
    }

    public void got(byte[] data) {
        // Do nothing
    }

    public void open() {
        // Do nothing
    }

    public void close() {
        // Do nothing
    }

    public WebsocketClient getWebsocket() {
        return websocket;
    }
}