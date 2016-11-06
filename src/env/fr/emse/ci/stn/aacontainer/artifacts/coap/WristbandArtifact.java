// CArtAgO artifact code for project swot_agents

package fr.emse.ci.stn.aacontainer.artifacts.coap;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import cartago.OPERATION;
import fr.emse.ci.stn.aacontainer.artifacts.STNClient;
import fr.emse.ci.stn.aacontainer.utils.ScenarioVocab;


public class WristbandArtifact extends CoAPArtifact {

    Model rdfModel;
    
	void init(String artifactUri) {
	    super.init(artifactUri, MediaTypeRegistry.APPLICATION_RDF_XML);
	    
	    rdfModel = ModelFactory.createDefaultModel();
	    
	    rdfModel.add(ResourceFactory.createResource(artifactUri), 
	            RDF.type, ScenarioVocab.SmartWristband);
	}
	
	@OPERATION
	void turnVibrationsOn() {
	    Model state = ModelFactory.createDefaultModel().add(rdfModel);
	    
	    state.add(ResourceFactory.createResource(artifactUri), 
                ScenarioVocab.vibrations, ScenarioVocab.StateOn);
	    
	    put(STNClient.modelToString(state, "RDF/XML"));
	}
	
	@OPERATION
    void turnVibrationsOff() {
	    Model state = ModelFactory.createDefaultModel().add(rdfModel);
        
	    state.add(ResourceFactory.createResource(artifactUri), 
                ScenarioVocab.vibrations, ScenarioVocab.StateOff);
        
        put(STNClient.modelToString(state, "RDF/XML"));
	}
}
