// CArtAgO artifact code for project swot_agents

package fr.emse.ci.stn.aacontainer.artifacts.notifications;


import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import cartago.Artifact;
import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;
import fr.emse.ci.stn.aacontainer.artifacts.STNClient;
import fr.emse.ci.stn.aacontainer.utils.STNCore;


public class WebSocketNotificationDispatcher extends Artifact {
    
    private boolean dispatcherRunning;
    
    public static final int NOTIFICATION_DELIVERY_DELAY = 10;
    
    private WebSocketClient client;
    private STNPlatformWebSocket socket;
    private URI webSocketUri;
    
    
	void init(String webSocketAddress) throws URISyntaxException {
        webSocketUri = new URI(webSocketAddress);
	    socket = new STNPlatformWebSocket();
	    client = new WebSocketClient();
	}
	
	@OPERATION
	void start() {
	    try {
	        dispatcherRunning = true;
	        execInternalOp("deliverNotifications");

	        client.start();
            client.connect(socket, webSocketUri, new ClientUpgradeRequest());
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	@OPERATION
	void stop() {
	    try {
            client.stop();
            dispatcherRunning = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	@OPERATION
    void registerToWS(String callbackUri) {
	    AgentRegistry.getInstance().put(callbackUri, getOpUserId());
	}
	
	
	private Model buildMessageModel(String sender, String receiver, String message) {
	    Model model = ModelFactory.createDefaultModel();
        
        model.add(ResourceFactory.createResource(""), RDF.type, STNCore.Message);
        model.add(ResourceFactory.createResource(""), STNCore.hasBody, message);
        
        model.add(ResourceFactory.createResource(""), 
                STNCore.hasSender, 
                ResourceFactory.createResource(sender)
            );
        
        if (receiver != null && !receiver.isEmpty()) {
            model.add(ResourceFactory.createResource(""), 
                    STNCore.hasReceiver, 
                    ResourceFactory.createResource(receiver)
                );
        }
        
        return model;
	}
	
	@OPERATION
	void sendMessage(String sender, String receiver, String message) {
	    Model model = buildMessageModel(sender, receiver, message);
	    
	    try {
            socket.sendMessage(STNClient.modelToString(model));
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	@OPERATION
	void publishMessage(String sender, String message) {
	    /*Model model = ModelFactory.createDefaultModel();
        
        model.add(ResourceFactory.createResource(""), RDF.type, STNCore.Message);
        model.add(ResourceFactory.createResource(""), STNCore.hasBody, message);
        
        model.add(ResourceFactory.createResource(""), 
                STNCore.hasSender, 
                ResourceFactory.createResource(sender)
            );*/
	    
	    Model model = buildMessageModel(sender, null, message);
	    
	    try {
	        socket.sendMessage(STNClient.modelToString(model));
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	@INTERNAL_OPERATION
	void deliverNotifications() {
        while (dispatcherRunning) {
            Notification notification = NotificationQueue.getInstance().poll();
            
            if (notification != null) {
                if (notification.agentId != null) {
//                    System.out.println("Signaling notification to " + notification.agentId.getAgentName()
//                                            + " with message: " + notification.message);
                    
                    signal(notification.agentId, "notification", notification.message);
                } else {
                    try {
                        
                        Number number = NumberFormat.getInstance(Locale.FRANCE).parse(notification.message);
                        
                        signal("outside_light_level", number.doubleValue());
                        
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                await_time(NOTIFICATION_DELIVERY_DELAY);
            }
        }
    }
}

