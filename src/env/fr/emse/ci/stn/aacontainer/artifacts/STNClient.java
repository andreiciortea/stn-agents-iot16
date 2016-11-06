// CArtAgO artifact code for project swot_agents

package fr.emse.ci.stn.aacontainer.artifacts;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;
import fr.emse.ci.stn.aacontainer.utils.STNCore;


public class STNClient extends Artifact {
    
    final static public String TURTLE = "TURTLE";
    final static public String TURTLE_MEDIA_TYPE = "text/turtle";
    
    private String platformUri;
    
    
	void init(String platformUri) {
	    this.platformUri = platformUri;
	}
	
	@OPERATION
	void registerToSTN(String webId, String callbackUri, 
	        String ownerWebId, OpFeedbackParam<String> accountUri) {
	    
	    Model account = ModelFactory.createDefaultModel();
	    
	    account.add(ResourceFactory.createResource(""), 
	            RDF.type, 
	            STNCore.UserAccount
            );
	    
	    if (webId != null && !webId.isEmpty()) {
    	    account.add(ResourceFactory.createResource(""), 
                    STNCore.heldBy, 
                    ResourceFactory.createResource(webId)
                );
	    }
	    
	    if (callbackUri != null && !callbackUri.isEmpty()) {
    	    account.add(ResourceFactory.createResource(""), 
    	            STNCore.callbackUri, 
    	            ResourceFactory.createResource(callbackUri)
                );
	    }
	    
	    if (ownerWebId != null && !ownerWebId.isEmpty()) {
    	    account.add(ResourceFactory.createResource(webId), 
                    STNCore.ownedBy, 
                    ResourceFactory.createResource(ownerWebId)
                );
	    }
	    
	    String accountLocation = post("/users/", TURTLE_MEDIA_TYPE, modelToString(account));
	    accountUri.set(accountLocation);
	}
	
	@OPERATION
	void getThingsOwnedBy(String ownerUri, OpFeedbackParam<Object []> things) {
        List<String> accounts = new ArrayList<String>();
        
        Map<String,String> params = new HashMap<String,String>();
        params.put("ownedBy", ownerUri);
        
        String responsePayload = get("/users/", params);
        
        if (responsePayload == null || responsePayload.isEmpty()) {
            things.set(accounts.toArray());
            return;
        }
        
        InputStream stream = new ByteArrayInputStream(responsePayload.getBytes(StandardCharsets.UTF_8));
        
        Model model = ModelFactory.createDefaultModel().read(stream, null, TURTLE);
        ResIterator iterator = model.listResourcesWithProperty(RDF.type);
        
        while (iterator.hasNext()) {
            Resource res = iterator.next();
            if (res != null && res.isURIResource()) {
                String account = ((Resource) res).getURI();
                accounts.add(account);
            }
        }
        
        things.set(accounts.toArray());
	}
	
	@OPERATION
    void connectTo(String sourceUri, String targetUri, OpFeedbackParam<String> relationUri) {
	    Model account = ModelFactory.createDefaultModel();
        
        account.add(ResourceFactory.createResource(""), RDF.type, STNCore.Relation);
        
        // TODO: validate source and target URIs
        account.add(ResourceFactory.createResource(""), 
                STNCore.source, 
                ResourceFactory.createResource(sourceUri)
            );
        
        account.add(ResourceFactory.createResource(""), 
                STNCore.target, 
                ResourceFactory.createResource(targetUri)
            );
        
        String relationLocation = post("/connections/", TURTLE_MEDIA_TYPE, modelToString(account));
        
        relationUri.set(relationLocation);
	}
	
	@OPERATION
    void getIncomingRelations(String targetUri, OpFeedbackParam<Object []> connections) {
	    List<String> followers = new ArrayList<String>();
	    
	    Map<String,String> params = new HashMap<String,String>();
	    params.put("target", targetUri);
	    
	    String responsePayload = get("/connections/", params);
	    
	    if (responsePayload == null || responsePayload.isEmpty()) {
	        connections.set(followers.toArray());
	        return;
	    }
	    
	    InputStream stream = new ByteArrayInputStream(responsePayload.getBytes(StandardCharsets.UTF_8));
	    
        Model model = ModelFactory.createDefaultModel().read(stream, null, TURTLE);
        NodeIterator iterator = model.listObjectsOfProperty(STNCore.source);
        
        while (iterator.hasNext()) {
            RDFNode node = iterator.next();
            if (node != null && node.isURIResource()) {
                String follower = ((Resource) node).getURI();
                followers.add(follower);
            }
        }
        
        connections.set(followers.toArray());
	}
	
	@OPERATION
	void postMessage(String sender, String message, OpFeedbackParam<String> messageUri) {
	    Model model = ModelFactory.createDefaultModel();
        
	    model.add(ResourceFactory.createResource(""), RDF.type, STNCore.Message);
	    model.add(ResourceFactory.createResource(""), STNCore.hasBody, message);
	    
	    model.add(ResourceFactory.createResource(""), 
	            STNCore.hasSender, 
	            ResourceFactory.createResource(sender)
            );
        
        String messageLocation = post("/messages/", TURTLE_MEDIA_TYPE, modelToString(model));
        messageUri.set(messageLocation);
	}
	
	
	private String get(String requestUri, Map<String,String> params) {
	    try {
    	    URIBuilder builder = new URIBuilder(platformUri + requestUri);
    	    
    	    for (Entry<String,String> entry : params.entrySet()) {
    	        builder.addParameter(entry.getKey(), entry.getValue());
    	    }
    	    
    	    HttpGet request = new HttpGet(builder.build());
    	    
    	    HttpClient client = new DefaultHttpClient();
    	    HttpResponse response = client.execute(request);
    	    
            return EntityUtils.toString(response.getEntity());
	    } catch (Exception e) {
            e.printStackTrace();
            return null;
        } 
	}
	
	private String post(String requestUri, String contentType, String payload) {
	    HttpPost request = new HttpPost(platformUri + requestUri);
	    
	    try {
	        request.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
            request.setEntity(new StringEntity(payload));

            HttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(request);
            
            Header artifactLocation = response.getFirstHeader(HttpHeaders.LOCATION);
            
            return (artifactLocation == null) ? null : artifactLocation.getValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
	}
	
	public static String modelToString(Model model) {
        return modelToString(model, TURTLE);
    }
    
	public static String modelToString(Model model, String serializationFormat) {
        StringWriter sw = new StringWriter();
        model.write(sw, serializationFormat);
        
        return sw.toString();
    }
}
