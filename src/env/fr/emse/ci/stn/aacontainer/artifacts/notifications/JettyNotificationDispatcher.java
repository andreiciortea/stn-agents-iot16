// CArtAgO artifact code for project swot_agents

package fr.emse.ci.stn.aacontainer.artifacts.notifications;


import java.io.IOException;
import java.util.AbstractQueue;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import cartago.AgentId;
import cartago.Artifact;
import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;


public class JettyNotificationDispatcher extends Artifact {
    
    private Map<String,AgentId> callbackUris;
    private AbstractQueue<Notification> notificationQueue;
    
    private Server server;
    private boolean httpServerRunning;
    
    public static final int NOTIFICATION_DELIVERY_DELAY = 10;
    
    
	void init(int port) {
	    server = new Server(port);
	    server.setHandler(new NotificationHandler());
	    
	    callbackUris = new Hashtable<String,AgentId>();
	    notificationQueue = new ConcurrentLinkedQueue<Notification>();
	}
	
	@OPERATION
	void start() {
	    try {
	        httpServerRunning = true;
	        execInternalOp("deliverNotifications");
            
	        server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	@OPERATION
	void stop() {
	    try {
            server.stop();
            httpServerRunning = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	@OPERATION
    void register(String callbackUri) {
//	    callbackUris.put(callbackUri, getCurrentOpAgentId()); // CArtAgO 2.0.2 API
	    callbackUris.put(callbackUri, getOpUserId());
	}
	
	@INTERNAL_OPERATION
	void deliverNotifications() {
        while (httpServerRunning) {
            Notification notification = notificationQueue.poll();
            
            if (notification != null) {
//                System.out.println("Signaling notification to " + notification.agentId.getAgentName()
//                        + " with message: " + notification.message);
                signal(notification.agentId, "notification", notification.message);
            } else {
                await_time(NOTIFICATION_DELIVERY_DELAY);
            }
        }
    }
	
	
	class NotificationHandler extends AbstractHandler {

	    @Override
	    public void handle(String target, Request baseRequest, HttpServletRequest request, 
	            HttpServletResponse response) throws IOException, ServletException {
            
            AgentId agentId = callbackUris.get(target);
            
            if (agentId == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain");
                response.getWriter().println("Resource not found!");
            } else {
                if (request.getMethod().equalsIgnoreCase("POST")) {
                    String payload = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                    notificationQueue.add(new Notification(agentId, payload));
                } else {
                    response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    response.setContentType("text/plain");
                    response.getWriter().println("Method not allowed!");
                }
            }
        }
	}
	
}

