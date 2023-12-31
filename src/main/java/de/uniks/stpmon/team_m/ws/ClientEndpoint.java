package de.uniks.stpmon.team_m.ws;


import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@javax.websocket.ClientEndpoint
public class ClientEndpoint {
    private final URI endpointURI;
    private final List<Consumer<String>> messageHandlers = Collections.synchronizedList(new ArrayList<>());

    Session userSession;

    public ClientEndpoint(URI endpointURI) {
        this.endpointURI = endpointURI;
    }

    public boolean isOpen() {
        return this.userSession != null && this.userSession.isOpen();
    }

    public void open() {
        if (isOpen()) {
            return;
        }

        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @OnOpen
    public void onOpen(Session userSession) {
        this.userSession = userSession;
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        this.userSession = null;
    }

    @OnMessage
    public void onMessage(String message) {
        for (final Consumer<String> handler : this.messageHandlers) {
            handler.accept(message);
        }
    }

    @OnError
    public void onError(Throwable error) {
        System.out.println("Error: " + error.getMessage());
    }

    public void addMessageHandler(Consumer<String> msgHandler) {
        this.messageHandlers.add(msgHandler);
    }

    public void removeMessageHandler(Consumer<String> msgHandler) {
        this.messageHandlers.remove(msgHandler);
    }

    public void sendMessage(String message) {
        if (this.userSession == null) {
            return;
        }
        this.userSession.getAsyncRemote().sendText(message);
    }

    public void close() {
        if (this.userSession == null) {
            return;
        }
        try {
            this.userSession.close();
        } catch (IOException e) {
            System.out.println("Error closing session: " + e.getMessage());
        }
    }

    public boolean hasMessageHandler() {
        return !this.messageHandlers.isEmpty();
    }
}

