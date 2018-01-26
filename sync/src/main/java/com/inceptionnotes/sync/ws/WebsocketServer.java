package com.inceptionnotes.sync.ws;

import com.inceptionnotes.sync.Server;
import com.inceptionnotes.sync.events.Event;

import java.util.HashSet;
import java.util.Set;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * Created by jacob on 1/20/18.
 */

public class WebsocketServer extends ServerEndpointConfig.Configurator {

    private final Server server;
    private final Set<WebsocketClient> sessions = new HashSet<>();

    public WebsocketServer() {
        server = new Server(this);
    }

    public void broadcast(WebsocketClient client, Event event) {
        synchronized (sessions) {
            for (WebsocketClient other : sessions) {
                if (!other.getSession().isOpen()) {
                    continue;
                }

                if (client.getSession().getId().equals(other.getSession().getId())) {
                    continue;
                }

                other.getClient().send(event);
            }
        }
    }

    public void join(WebsocketClient client) {
        synchronized (sessions) {
            sessions.add(client);
        }
    }

    public void leave(WebsocketClient client) {
        synchronized (sessions) {
            sessions.remove(client);
        }
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig conf, HandshakeRequest req, HandshakeResponse resp) {
        conf.getUserProperties().put("server", server);
    }
}
