// Agent wristband in project swot_agents

/* Initial beliefs and rules */

owner(david).

alarm_type(vibrations).

function_mode(night)[source(percept)].


/* Initial goals */

!start.

/* Plans */

+!start : owner(Owner) <-
    !setup;
    // Wait a bit a kick off the interaction by posting that the owner is asleep.
    .wait(4000);
    !notifyOwnerAsleep.


// Create a CoAP-based wristband artifact (device URI learned from container mamanger).
+device_uri(DeviceUri) : true <- !createWristbandArtifact(DeviceUri).

+!createWristbandArtifact(DeviceUri) : true <-
    makeArtifact("wristband", "fr.emse.ci.stn.aacontainer.artifacts.coap.WristbandArtifact", [DeviceUri], _).

// Artifact was created previously, fail gracefully.
-!createWristbandArtifact(_) : true .


// Receive CFP and send proposal.
+!processMessage(SenderUri, tell, cfp(CNPId, achieve(not asleep(Owner)))) : proposal(CNPId).

+!processMessage(SenderUri, tell, cfp(CNPId, achieve(not asleep(Owner)))) 
    : owner(Owner) & my_account_uri(MyAccountUri) & alarm_type(AlarmType) <-
    .concat("tell propose(", CNPId, ", alarm_type(", AlarmType, "))", Proposal);
    .print("Sending proposal [CNP ", CNPId, "] : ", Proposal);
    +proposal(CNPId);
    sendMessage(MyAccountUri, SenderUri, Proposal).


// Accept proposal and fulfill obligations.
+!processMessage(SenderUri, tell, accept_proposal(CNPId)) : my_account_uri(MyAccountUri) & alarm_type(AlarmType) <-
        .print("I won the CNP, attempting to wake up owner...");
        !turnVibrationsOn(SenderUri, CNPId).


// Turn on vibrations and announce the task has been performed.
+!turnVibrationsOn(SenderUri, CNPId) : my_account_uri(MyAccountUri) <-
    turnVibrationsOn;
    .concat("tell inform_done(", CNPId, ")", Message);
    sendMessage(MyAccountUri, SenderUri, Message).

// If the action fails (e.g., CoAP device emulator unreachable), inform the CNP initiator.
-!turnVibrationsOn(SenderUri, CNPId)[error_msg(Error)] : my_account_uri(MyAccountUri) <-
    .print(Error);
    .concat("tell failure(", CNPId, ")", Message);
    sendMessage(MyAccountUri, SenderUri, Message).


// Notify all followers that the owner is asleep. 
+!notifyOwnerAsleep : my_account_uri(MyAccountUri) & owner(Owner) <-
    .concat("tell asleep(", Owner, ")[certainty(0.6),source(wristband)]", Message);
    .print(Owner, " is asleep, posting message to the STN: ", Message);
    publishMessage(MyAccountUri, Message).

// Notify all followers that the owner woke up.
+!notifyOwnerAwake: my_account_uri(MyAccountUri) & owner(Owner) <-
    .concat("untell asleep(", Owner, ")", Message);
    .print(Owner, " woke up, posting message to the STN: ", Message);
    publishMessage(MyAccountUri, Message).


{ include("inc/stn-common.asl") }
