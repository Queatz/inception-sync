package com.inceptionnotes.sync.ws;


import com.inceptionnotes.sync.Client;
import com.inceptionnotes.sync.Server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import static java.util.concurrent.TimeUnit.MINUTES;

@ServerEndpoint(value = "/ws", configurator = WebsocketServer.class)
public class WebsocketClient {

    private Session session;
    private Server server;
    private Client client;

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        Logger.getAnonymousLogger().info("WEBSOCKET (SESSION): " + session.getId());
        this.session = session;

        session.setMaxIdleTimeout(MINUTES.toMillis(30));
        server = (Server) endpointConfig.getUserProperties().get("server");
        client = new Client(this);
        server.join(client);
    }

    @OnClose
    public void onClose() {
        if (session.isOpen()) {
            try {
                session.close();
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }
        }

        Logger.getAnonymousLogger().info("WEBSOCKET: END");
        server.leave(client);
        client.close();
    }

    @OnMessage
    public void onMessage(String message) throws IOException {
        Logger.getAnonymousLogger().info("WEBSOCKET (MESSAGE): " + (message.length() > 128 ? message.substring(0, 127) + "..." + message.length() : message));
        client.got(message);
    }

    @OnMessage
    public void onData(byte[] data) throws IOException {
        Logger.getAnonymousLogger().info("WEBSOCKET (DATA): " + data.length);
        client.got(data);
    }

    @OnError
    public void onError(Throwable t) throws Throwable {
        Logger.getAnonymousLogger().info("WEBSOCKET (ERROR): " + t.getMessage());
        t.printStackTrace();
    }

    public Session getSession() {
        return session;
    }

    public Client getClient() {
        return client;
    }

    public Server getServer() {
        return server;
    }
}