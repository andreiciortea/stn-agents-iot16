// Agent lights in project swot_agents

/* Initial beliefs and rules */

owner(david).

curtains_state(closed).

daytime(true).

alarm_type(artificial_light).

{ include("inc/config.asl") }


/* Initial goals */

!start.


/* Plans */

+!start : use_philips_hue(true) <-
    !setup;
    !createHueArtifact.

+!createHueArtifact : philips_hue_light_uri(HueUri) <-
    // Create a Philips Hue artifact (if used).
    makeArtifact("lights", "fr.emse.ci.stn.aacontainer.artifacts.devices.HueArtifact", [HueUri], _).

// Philips Hue artifact was created previously, fail gracefully.
-!createHueArtifact : true .

// Start without hue.
+!start : true <- !setup.


// Create a CoAP-based wristband artifact (device URI learned from container mamanger).
+device_uri(DeviceUri) : use_philips_hue(false) <- !createCoAPLightsArtifact(DeviceUri).

+!createCoAPLightsArtifact(DeviceUri) : true <-
    makeArtifact("lights", "fr.emse.ci.stn.aacontainer.artifacts.coap.LightsArtifact", [DeviceUri], _).

// Artifact was created previously, fail gracefully.
-!createCoAPLightsArtifact(_) : true .


// Learn that owner is asleep (info posted to the STN by another agent).
+!processMessage(SenderUri, tell, asleep(Owner)[certainty(C),source(DeviceType)]) : owner(Owner) <-
    +asleep(Owner)[certainty(C),source(DeviceType)].

// Learn that the curtains are open (info posted to the STN by the window curtains agent).
+!processMessage(SenderUri, tell, curtains_state(State)) : true <-
    -+curtains_state(State).
    
// Learn outside light level (info posted to the STN by outside light sensor agent).
+!processMessage(SenderUri, tell, outside_light_level(Level)) : Level > 100 <-
    -+daytime(true).
    
+!processMessage(SenderUri, tell, outside_light_level(Level)) : true <-
    -+daytime(false).


// Receive CFP and send proposal.
+!processMessage(SenderUri, tell, cfp(CNPId, achieve(not asleep(Owner))))
    // Triggering context checks if it makes sense to make the proposal
    : owner(Owner) & (daytime(false) | (curtains_state(closed) & daytime(true)))
        & my_account_uri(MyAccountUri) & alarm_type(AlarmType) <-
            .concat("tell propose(", CNPId, ", alarm_type(", AlarmType, "))", Proposal);
            .print("Sending proposal [CNP ", CNPId, "] : ", Proposal);
            sendMessage(MyAccountUri, SenderUri, Proposal).

// Handle CFP if not applicable in the current bedroom context.
+!processMessage(SenderUri, tell, cfp(CNPId, achieve(not asleep(Owner))))
    : owner(Owner) & curtains_state(open) & daytime(true) <-
        .print("The curtains are open and it's bright outside, I'll sit this one out.").

+!processMessage(SenderUri, tell, cfp(CNPId, achieve(not asleep(Owner)))) : true <-
    .print("Received CFP, but no applicable context !!!").


// Accept proposal and fulfill obligations.
+!processMessage(SenderUri, tell, accept_proposal(CNPId)) : my_account_uri(MyAccountUri) <-
    .print("I won the CNP, attempting to wake up owner...");
    !turnLightsOn(SenderUri, CNPId).


// Turn on the lights and announce the task has been performed.
+!turnLightsOn(SenderUri, CNPId) : my_account_uri(MyAccountUri) <-
    turnLightsOn;
    .concat("tell inform_done(", CNPId, ")", Message);
    sendMessage(MyAccountUri, SenderUri, Message).

// If the action fails (e.g., device unreachable), inform the CNP initiator.
-!turnLightsOn(SenderUri, CNPId)[error_msg(Error)] : my_account_uri(MyAccountUri) <-
    .print(Error);
    .concat("tell failure(", CNPId, ")", Message);
    sendMessage(MyAccountUri, SenderUri, Message).


{ include("inc/stn-common.asl") }
