package fr.emse.ci.stn.aacontainer.utils;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class ScenarioVocab {

    private static final String PREFIX = "http://example.org#";

    public static Resource resource(String fragment) {
        return ResourceFactory.createResource(PREFIX + fragment);
    }
    
    public static Property property(String fragment) {
        return ResourceFactory.createProperty(PREFIX + fragment);
    }
    
    public static final Resource SmartWristband             = resource("SmartWristband");
    public static final Resource SmartWindowCurtains        = resource("SmartWindowCurtains");
    public static final Resource SmartMattressCover         = resource("SmartMattressCover");
    public static final Resource SmartLights                = resource("SmartLights");
    public static final Resource LightSensor                = resource("LightSensor");

    public static final Property vibrations                 = property("vibrations");
    
    public static final Property hasState                   = property("hasState");
    
    public static final Resource StateOn                    = resource("StateOn");
    public static final Resource StateOff                   = resource("StateOff");
    
    public static final Resource StateOpen                  = resource("StateOpen");
    public static final Resource StateClosed                = resource("StateClosed");
}
