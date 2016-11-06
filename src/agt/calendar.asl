// Agent calendar in project swot_agents

/* Initial beliefs and rules */

owner(david).

// In this scenario implementation, David is a very busy person. He always
// has an upcoming event.
upcoming_event(true).

// Beliefs about the owner's wake up preferences (lower rank is better).
alarm_rank(vibrations, 0).
alarm_rank(natural_light, 2).
alarm_rank(artificial_light, 1).

callback_uri("http://localhost:58880/calendar/").


/* Initial goals */

!start.


/* Plans */

+!start : true <-
    .print("Hello world!");
    // Focus on artifacts, register to the STN, connect to other agents.
    !setup;
    // Start monitoring scheduled events.
    !monitor_events.


// This plan is triggered if there is an upcoming event and it is known that the owner is asleep.
+!monitor_events : upcoming_event(true) & owner(Owner) & asleep(Owner)[certainty(C)] & C > 0.5 <-
    .print(Owner, " is asleep and there is an upcoming event");
    jia.currentTimeMillis(CNPId);
    !launchCFP.

// If not, keep monitoring.
+!monitor_events : true <-
    .wait(1000);
    !monitor_events.


/* Launch and handle CNP */

// Start the CNP by launching a CFP.
+!launchCFP : my_account_uri(MyAccountUri) & owner(Owner) <-
    jia.currentTimeMillis(CNPId);
    .print("Launching CNP ", CNPId);
    .concat("tell cfp(", CNPId, ", achieve(not asleep(", Owner, ")))", Message);
    // Post the message to the STN.
    publishMessage(MyAccountUri, Message);
    // Wait for 2s to receive proposals...
    .wait(2000);
    // ... and then close the interaction.
    !closeCNP(CNPId).

+!closeCNP(CNPId) : true <-
    .findall(proposal(CNPId, SenderUri, AlarmType), proposal(CNPId, SenderUri, AlarmType), List);
    // Check that there are proposals.
    List \== [];
    .length(List, L);
    .print("Got ", L, " proposals for CNP ", CNPId, "!");
    ?proposal(CNPId, Sender, Alarm);
    // Select winning proposal
    !selectProposal(List, proposal(CNPId, Sender, Alarm), proposal(CNPId, WinnerUri, WinnerAlarmType));
    // Announce the winner
    !informWinner(CNPId, WinnerUri, WinnerAlarmType).

// Plan fails if the calendar did not receive any proposals.
-!closeCNP(CNPId) : true <-
    .print("CNP ", CNPId, " received no proposals, launching a new one shortly...");
    .wait(2000);
    !launchCFP.

+!informWinner(CNPId, WinnerUri, WinnerAlarmType) : my_account_uri(MyAccountUri) <-
    .concat("tell accept_proposal(", CNPId, ")", Message);
    sendMessage(MyAccountUri, WinnerUri, Message);
    // Wait for 2s and check if the CNP was completed (or something went wrong, e.g. contractor died).
    .wait(2000);
    !checkCNPCompleted(CNPId, WinnerAlarmType).


+!checkCNPCompleted(CNPId, WinnerAlarmType) : owner(Owner) & asleep(Owner) <-
    .print("Owner asleep (no wake up confirmation), last failed alarm: ", WinnerAlarmType , ". Launching a new CNP shortly...");
    +failed_alarm(WinnerAlarmType);
    .wait(3000);
    !launchCFP.

+!checkCNPCompleted(CNPId, WinnerAlarmType) : true .


/* Proposal selection logic */

// If we are finished, return the best proposal.
+!selectProposal([], BestOffer, BestOffer) : true .

// This alarm type failed before.
+!selectProposal([proposal(_,_,AlarmType) | Tail], CurrentBest, BestOffer) : failed_alarm(AlarmType) <-
    !selectProposal(Tail, CurrentBest, BestOffer).

// Check if proposal is better than the current best.
+!selectProposal([proposal(CNPId, SenderUri1, AlarmType1) | Tail], proposal(CNPId, SenderUri2, AlarmType2), BestOffer) 
    : alarm_rank(AlarmType1, Rank1) & alarm_rank(AlarmType2, Rank2) & Rank1 < Rank2 <-
        !selectProposal(Tail, proposal(CNPId, SenderUri1, AlarmType1), BestOffer).

// Check if current best failed previously.
+!selectProposal([proposal(CNPId, SenderUri1, AlarmType1) | Tail], proposal(CNPId, SenderUri2, AlarmType2), BestOffer) 
    : failed_alarm(AlarmType2) <-
        !selectProposal(Tail, proposal(CNPId, SenderUri1, AlarmType1), BestOffer).

// If not, stick with the current best.
+!selectProposal([proposal(CNPId, SenderUri, AlarmType) | Tail], CurrentBest, BestOffer) : true <-
    !selectProposal(Tail, CurrentBest, BestOffer).


/* Process CNP messages */

@contract[atomic]
+!processMessage(SenderUri, tell, propose(CNPId, alarm_type(AlarmType))) : my_account_uri(MyAccountUri) <-
    +proposal(CNPId, SenderUri, AlarmType).

+!processMessage(SenderUri, tell, inform_done(CNPId)) : owner(Owner) <-
    .print("CNP ", CNPId , " completed.");
    +done(CNPId).

+!processMessage(SenderUri, tell, failure(CNPId)) : true <-
    // Remember that this alarm type failed to wake up the owner.
    .print("CNP ", CNPId , " failed, contractor did not perform its task.");
    +done(CNPId).

+!processMessage(SenderUri, tell, asleep(Owner)[certainty(C),source(DeviceType)]) : owner(Owner) <-
    +asleep(Owner)[certainty(C),source(DeviceType)].

+!processMessage(SenderUri, untell, asleep(Owner)) : owner(Owner) <-
    .print(Owner, " woke up!");
    -asleep(Owner).


{ include("inc/stn-common.asl") }
