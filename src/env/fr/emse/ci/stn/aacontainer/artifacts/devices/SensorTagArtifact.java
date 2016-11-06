// CArtAgO artifact code for project swot_agents

package fr.emse.ci.stn.aacontainer.artifacts.devices;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import cartago.Artifact;
import fr.emse.ci.stn.aacontainer.artifacts.notifications.Notification;
import fr.emse.ci.stn.aacontainer.artifacts.notifications.NotificationQueue;

public class SensorTagArtifact extends Artifact implements MqttCallback {
    
	void init(String brokerAddress, String clientId, 
	        String topic, String username, String password) {
	    
	    MemoryPersistence persistence = new MemoryPersistence();
	    
	    try {
            MqttClient client = new MqttClient(brokerAddress, clientId, persistence);
            
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            
            if (username != null && !username.isEmpty()) {
                connOpts.setUserName(username);
                connOpts.setPassword(password.toCharArray());
            }
            
            client.setCallback(this);
            client.connect(connOpts);
            
            client.subscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
	}
	
    public void connectionLost(Throwable arg0) {
        // TODO Auto-generated method stub
    }

    public void deliveryComplete(IMqttDeliveryToken arg0) {
        // TODO Auto-generated method stub
    }
    
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        
        JsonParser parser = new JsonParser();
        JsonElement lightLevel = parser.parse(payload).getAsJsonObject().getAsJsonObject("d").get("light");
        
        if (lightLevel != null) {
            NotificationQueue.getInstance().add(new Notification(lightLevel.getAsString()));
        }
    }
}

