package fr.emse.ci.stn.aacontainer.artifacts.notifications;

import cartago.AgentId;

public class Notification {

    public AgentId agentId;
    public String message;
    
    public Notification(String message) {
        this(null, message);
    }
    
    public Notification(AgentId agentId, String message) {
        this.agentId = agentId;
        this.message = message;
    }
}
