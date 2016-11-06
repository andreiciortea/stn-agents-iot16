// CArtAgO artifact code for project swot_agents

package fr.emse.ci.stn.aacontainer.artifacts.notifications;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import cartago.Artifact;
import cartago.OPERATION;
import fr.emse.ci.stn.aacontainer.artifacts.STNClient;
import fr.emse.ci.stn.aacontainer.utils.STNCore;


public class NotificationInterpreter extends Artifact {
	
	@OPERATION
	void interpretNotification(String notification) {
	    InputStream stream = new ByteArrayInputStream(notification.getBytes(StandardCharsets.UTF_8));
	    Model model = ModelFactory.createDefaultModel().read(stream, null, STNClient.TURTLE);
	    
        if (model.contains(null, RDF.type, STNCore.Relation)) {
            interpretRelation(model);
        } else if (model.contains(null, RDF.type, STNCore.Message)) {
            interpretMessage(model);
        }
	}
	
	private void interpretRelation(Model model) {
	    Resource source = (Resource) model.getRequiredProperty(null, STNCore.source).getObject();
        Resource target = (Resource) model.getRequiredProperty(null, STNCore.target).getObject();
        
        if (source.isURIResource() && target.isURIResource()) {
            ResIterator iterator = model.listResourcesWithProperty(RDF.type, STNCore.Relation);
            
            if (iterator.hasNext()) {
                Resource relation = iterator.next();
                if (relation.isURIResource()) {
                    signal(this.getOpUserId(), 
                            "newFollower", relation.getURI(), source.getURI(), target.getURI());
                }
            }
        }
	}
	
	private void interpretMessage(Model model) {
	    Resource sender = (Resource) model.getRequiredProperty(null, STNCore.hasSender).getObject();
	    NodeIterator iterator = model.listObjectsOfProperty(STNCore.hasBody);
	    
	    if (sender.isURIResource()) {
    	    if (iterator.hasNext()) {
    	        String message = iterator.next().asLiteral().getString();
    	        String[] contents = message.split(" ", 2);
    	        
    	        if (contents.length == 2) {
    	            signal(getOpUserId(), "newMessage", sender.getURI(), contents[0], contents[1]);
    	        }
    	    }
	    }
	}
}

