// CArtAgO artifact code for project swot_agents

package fr.emse.ci.stn.aacontainer.artifacts.devices;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import cartago.Artifact;
import cartago.OPERATION;


public class HueArtifact extends Artifact {
    
    private final int UNDEFINED = -1;
    
    String lightbulbUri;
    
    
	void init(String uri) {
	    this.lightbulbUri = uri;
	}
	
	private int setState(String state) {
	    HttpPut request = new HttpPut(lightbulbUri);
        
        try {
            
            request.setEntity(new StringEntity(state));
            
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(request);
            
            return response.getStatusLine().getStatusCode();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return UNDEFINED;
	}
	
	@OPERATION
	void turnLightsOn() {
	    setState("{ \"on\" : true }");
	}
	
	@OPERATION
	void turnLightsOff() {
	    setState("{ \"on\" : false }");
	}
}

