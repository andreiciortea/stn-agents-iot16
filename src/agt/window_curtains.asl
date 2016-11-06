// Agent window_curtains in project swot_agents

/* Initial beliefs and rules */

owner(david).

curtains_state(closed).

outside_light_level(120).
daytime(true).

alarm_type(natural_light).


/* Initial goals */

!start.


/* Plans */

+!start : true <- !setup.


// Create a CoAP-based wristband artifact (device URI learned from container mamanger).
+device_uri(DeviceUri) : true <- !createCurtainsArtifact(DeviceUri).

+!createCurtainsArtifact(DeviceUri) : true <-
    makeArtifact("window_curtains", "fr.emse.ci.stn.aacontainer.artifacts.coap.WindowCurtainsArtifact", [DeviceUri], _).

// Artifact was created previously, fail gracefully.
-!createCurtainsArtifact(_) : true .


// Learn that owner is asleep (info posted to the STN by another agent).
+!processMessage(SenderUri, tell, asleep(Owner)[certainty(C),source(DeviceType)]) : owner(Owner) <-
    +asleep(Owner)[certainty(C),source(DeviceType)].

// Learn outside light level (info posted to the STN by outside light sensor agent).
+!processMessage(SenderUri, tell, outside_light_level(Level)) : Level > 100 <-
    -+outside_light_level(Level);
    -+daytime(true).
    
+!processMessage(SenderUri, tell, outside_light_level(Level)) : true <-
    -+outside_light_level(Level);
    -+daytime(false).


// Receive CFP and send proposal.
+!processMessage(SenderUri, tell, cfp(CNPId, achieve(not asleep(Owner))))
    // Triggering context checks if it makes sense to make the proposal 
    : owner(Owner) & curtains_state(closed) & daytime(true)
        & my_account_uri(MyAccountUri) & alarm_type(AlarmType) <-
            .concat("tell propose(", CNPId, ", alarm_type(", AlarmType, "))", Proposal);
            .print("Sending proposal [CNP ", CNPId, "] : ", Proposal);
            sendMessage(MyAccountUri, SenderUri, Proposal).

// The following plans handle the CFP if the current state of the bedroom prevent the curtains to participate.
+!processMessage(SenderUri, tell, cfp(CNPId, achieve(not asleep(Owner)))) : owner(Owner) & curtains_state(open) <-
    .print("Curtains are already open.").

+!processMessage(SenderUri, tell, cfp(CNPId, achieve(not asleep(Owner)))) : owner(Owner) & daytime(false) & outside_light_level(Level) <-
    .print("It's dark outside (", Level, " lux), I'll sit this one out.").


// Accept proposal and fulfill obligations.
+!processMessage(SenderUri, tell, accept_proposal(CNPId)) : my_account_uri(MyAccountUri) <-
    .print("I won the CNP, attempting to wake up owner...");
    !openCurtains(SenderUri, CNPId).


// Open the curtains and announce the task has been performed.
+!openCurtains(SenderUri, CNPId) : my_account_uri(MyAccountUri) <-
    openCurtains;
    -+curtains_state(open);
    // Inform other agents that the curtains are now open.
    publishMessage(MyAccountUri, "tell curtains_state(open)");
    // Inform that the CNP task was performed.
    .concat("tell inform_done(", CNPId, ")", Message);
    sendMessage(MyAccountUri, SenderUri, Message).

// If the action fails (e.g., CoAP device emulator unreachable), inform the CNP initiator.
-!openCurtains(SenderUri, CNPId)[error_msg(Error)] : my_account_uri(MyAccountUri) <-
    .print(Error);
    .concat("tell failure(", CNPId, ")", Message);
    sendMessage(MyAccountUri, SenderUri, Message).


{ include("inc/stn-common.asl") }
