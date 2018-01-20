package com.inceptionnotes.sync.ws;

import com.inceptionnotes.sync.Server;

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

    public void broadcast(WebsocketClient client, String message) {
        synchronized (sessions) {
            for (WebsocketClient other : sessions) {
                if (!other.getSession().isOpen()) {
                    continue;
                }

                if (client.getSession().getId().equals(other.getSession().getId())) {
                    continue;
                }

                other.getClient().send(message);
            }
        }
    }

    public void join(WebsocketClient chat) {
        synchronized (sessions) {
            sessions.add(chat);
        }
    }

    public void leave(WebsocketClient chat) {
        synchronized (sessions) {
            sessions.remove(chat);
        }
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig conf, HandshakeRequest req, HandshakeResponse resp) {
        conf.getUserProperties().put("server", server);
    }
}
