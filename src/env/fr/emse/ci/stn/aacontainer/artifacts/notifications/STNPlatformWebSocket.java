package fr.emse.ci.stn.aacontainer.artifacts.notifications;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.google.gson.Gson;

import cartago.AgentId;


@WebSocket
public class STNPlatformWebSocket {
    
    private Session session;
    
    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
    }
    
    @OnWebSocketMessage
    public void onMessage(String messageJson) {
        AgentMessage message = (new Gson()).fromJson(messageJson, AgentMessage.class);
        AgentId receiverId = AgentRegistry.getInstance().get(message.receiver);
        
        NotificationQueue.getInstance().add(new Notification(receiverId, message.body));
    }
    
    public void sendMessage(String message) {
        try {
            session.getRemote().sendString(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    class AgentMessage {
        public String receiver;
        public String body;
        
        AgentMessage(String receiver, String body) {
            this.receiver = receiver;
            this.body = body;
        }
    }
    
}
