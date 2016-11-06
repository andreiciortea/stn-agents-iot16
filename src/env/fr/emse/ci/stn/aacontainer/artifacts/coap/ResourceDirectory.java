// CArtAgO artifact code for project swot_agents

package fr.emse.ci.stn.aacontainer.artifacts.coap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.LinkFormat;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;


public class ResourceDirectory extends Artifact {
    
    private String rdUri;
    
    
	void init(String rdUri) {
	    this.rdUri = rdUri;
	}
	
	
	private List<String []> extractTypedResources(String payload) {
	    List<String[]> resources = new ArrayList<String []>();
	    
	    Set<WebLink> links = LinkFormat.parse(payload);
	    
	    for (WebLink link : links) {
	        List<String> resourceTypes = link.getAttributes().getResourceTypes();
	        
	        if (resourceTypes != null && !resourceTypes.isEmpty()) {
	            List<String> cons = link.getAttributes().getAttributeValues("con");
	            
	            // If a context was registered, build the correct URI
	            if (cons != null && !cons.isEmpty()) {
	                String context = cons.get(0);
	                
                    try {
                        
                        URI registeredUri = new URI(link.getURI());
                        String  resourcePath = registeredUri.getPath();
                        
                        resources.add(new String[] {context + resourcePath, resourceTypes.get(0)});
                        
                    } catch (URISyntaxException e) {
                        resources.add(new String[] {link.getURI(), resourceTypes.get(0)});
                    }
	            } else {
	                resources.add(new String[] {link.getURI(), resourceTypes.get(0)});
	            }
	        }
	    }
	    
	    return resources;
	}
	
	
	@OPERATION
	void getResources(OpFeedbackParam<Object []> resources) {
	    CoapClient client = new CoapClient(rdUri + "/rd-lookup/res");
	    CoapResponse response = client.get();
	    
	    if (response != null && (response.getCode() == ResponseCode.CONTENT)) {
	        resources.set(extractTypedResources(response.getResponseText()).toArray());
	    } else {
	        resources.set((new ArrayList<String []>()).toArray());
	    }
	}
	
}
