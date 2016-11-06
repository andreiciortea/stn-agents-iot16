// CArtAgO artifact code for project swot_agents

package fr.emse.ci.stn.aacontainer.artifacts.coap;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import cartago.Artifact;


public class CoAPArtifact extends Artifact {
    
    protected String artifactUri;
    protected int defaultMediaType;
    
    /*
     * This method is called by the CArtAgO framework when the artifact is created.
     */
    void init(String artifactUri, int defaultMediaType) {
        this.artifactUri = artifactUri;
        this.defaultMediaType = defaultMediaType;
    }
    
    void init(String artifactUri) {
        init(artifactUri, MediaTypeRegistry.UNDEFINED);
    }
    
    protected CoapResponse get() {
        return (new CoapClient(artifactUri)).get();
    }
    
    protected void post(int format) {
        // TODO
    }
    
    protected ResponseCode put(String data) {
        return put(data, defaultMediaType);
    }
    
    protected ResponseCode put(String data, int format) {
        CoapResponse response = (new CoapClient(artifactUri)).put(data, format);
        
        if (response == null) {
            failed("Could not reach device emulator.");
            return null;
        }
        
        return response.getCode();
    }
    
    protected void delete() {
        // TODO
    }
    
}
