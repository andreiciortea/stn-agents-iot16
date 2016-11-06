// CArtAgO artifact code for project swot_agents

package fr.emse.ci.stn.aacontainer.artifacts.notifications;


import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.AbstractQueue;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.apache.http.ConnectionClosedException;
import org.apache.http.ExceptionLogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;

import cartago.AgentId;
import cartago.Artifact;
import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;


public class ApacheNotificationDispatcher extends Artifact {
    
    private Map<String,AgentId> callbackUris;
    private AbstractQueue<Notification> notificationQueue;
    
    private HttpServer server;
    private boolean httpServerRunning;
    
    public static final int NOTIFICATION_DELIVERY_DELAY = 10;
    
    
	void init(int port) {
	    
	    SocketConfig socketConfig = SocketConfig.custom()
                .setTcpNoDelay(true)
                .setSoKeepAlive(true)
                .build();
	    
	    server = ServerBootstrap.bootstrap()
                    .setListenerPort(port)
                    .setSocketConfig(socketConfig)
                    .setExceptionLogger(new StdErrorExceptionLogger())
                    .setServerInfo("Test/1.1")
                    .registerHandler("*", new NotificationHandler())
                    .create();
	    
	    callbackUris = new Hashtable<String,AgentId>();
	    notificationQueue = new ConcurrentLinkedQueue<Notification>();
	}
	
	@OPERATION
	void start() {
	    try {
	        httpServerRunning = true;
	        execInternalOp("deliverNotifications");
	        
            server.start();
            
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    server.shutdown(5, TimeUnit.SECONDS);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@OPERATION
	void stop() {
	    httpServerRunning = false;
	    server.shutdown(5, TimeUnit.SECONDS);
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
	
	
	class NotificationHandler implements HttpRequestHandler {

        public void handle(HttpRequest request, HttpResponse response, 
                HttpContext context) throws IOException {
            
            String requestUri = request.getRequestLine().getUri();
            AgentId agentId = callbackUris.get(requestUri);
            
            if (agentId == null) {
                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                response.setEntity(new StringEntity("Resource not found!"));
            } else {
                String method = request.getRequestLine()
                                        .getMethod()
                                        .toUpperCase(Locale.ROOT);
                
                if (method.equals("POST")) {
                    HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                    String payload = EntityUtils.toString(entity);
                    notificationQueue.add(new Notification(agentId, payload));
                } else {
                    response.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
                    response.setEntity(new StringEntity("Method not allowed!"));
                }
            }
        }
	}
	
	static class StdErrorExceptionLogger implements ExceptionLogger {

//        @Override
        public void log(final Exception ex) {
            if (ex instanceof SocketTimeoutException) {
                System.err.println("Connection timed out");
            } else if (ex instanceof ConnectionClosedException) {
                System.err.println(ex.getMessage());
            } else {
                ex.printStackTrace();
            }
        }

    }
}

