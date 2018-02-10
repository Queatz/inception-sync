package com.inceptionnotes.sync;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.inceptionnotes.sync.events.Event;
import com.inceptionnotes.sync.events.SyncEvent;
import com.inceptionnotes.sync.objects.Note;
import com.inceptionnotes.sync.store.NoteStore;
import com.inceptionnotes.sync.store.PropSet;
import com.inceptionnotes.sync.ws.WebsocketClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by jacob on 1/20/18.
 */

public class Client {

    private WebsocketClient websocket;
    private NoteStore noteStore;
    private World world;

    // Track client state
    private String show;
    private String personToken;
    private String clientToken;
    private String personId;
    private String clientId;

    public Client(WebsocketClient websocketClient) {
        this.websocket = websocketClient;
        world = websocket.getServer().getWorld();
        this.noteStore = new NoteStore();
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

    public void identify(String person, String client) {
        this.personToken = person;
        this.clientToken = client;

        // TODO convert personToken to vlllageId first here
        personId = noteStore.getPerson(personToken).getId();
        clientId = noteStore.getClient(personId, clientToken).getId();
    }

    public void setShow(String show) {
        this.show = show;
        sendUpdatedPropsFromShow();
    }

    public String getShow() {
        return show;
    }

    public String getPersonToken() {
        return personToken;
    }

    public String getClientToken() {
        return clientToken;
    }

    public String getPersonId() {
        return personId;
    }

    public String getClientId() {
        return clientId;
    }

    public NoteStore getNoteStore() {
        return noteStore;
    }

    public boolean isIdentified() {
        return clientToken != null && clientId != null && personToken != null && personId != null;
    }

    /**
     * Send updated props to client that the client has not seen yet
     */
    private void sendUpdatedPropsFromShow() {
        SyncEvent syncEvent = new SyncEvent();
        syncEvent.notes = new ArrayList<>();

        for (PropSet propSet : noteStore.changesUnderNoteForClientToken(clientId, show, personId)) {
            Note note = new Note();
            note.setId(propSet.getNoteId());

            propSet.getProps().forEach(noteProp -> {
                note.setProp(noteProp.getType(), noteProp.getValue());
                noteStore.setPropSeenByClient(clientId, noteProp.getId());
            });

            syncEvent.notes.add(note);
        }

        send(syncEvent);
    }

    public World getWorld() {
        return world;
    }
}