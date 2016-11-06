package fr.emse.ci.stn.aacontainer.artifacts.notifications;

import java.util.Hashtable;
import java.util.Map;

import cartago.AgentId;


public class AgentRegistry {
    
    private static AgentRegistry registry;
    private Map<String,AgentId> agentIds;

    
    private AgentRegistry() {
        agentIds = new Hashtable<String,AgentId>();
    }
    
    public static synchronized AgentRegistry getInstance() {
        if (registry == null) {
            registry = new AgentRegistry();
        }
        
        return registry;
    }
    
    public AgentId get(String callback) {
        return agentIds.get(callback);
    }
    
    public AgentId put(String callback, AgentId agentId) {
        return agentIds.put(callback, agentId);
    }
}
