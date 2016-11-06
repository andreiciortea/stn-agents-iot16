// Agent mattress_cover in project swot_agents

/* Initial beliefs and rules */

owner(david).

/* Initial goals */

!start.

/* Plans */

+!start : true <-
    !setup;
    // Wait a bit a kick off the interaction by posting that the owner is asleep.
    .wait(4000);
    !notifyOwnerAsleep.

+!notifyOwnerAsleep : my_account_uri(MyAccountUri) & owner(Owner) <-
    .concat("tell asleep(", Owner, ")[certainty(0.8),source(mattress_cover)]", Message);
    .print(Owner, " is asleep, posting message to the STN: ", Message);
    publishMessage(MyAccountUri, Message).



{ include("inc/stn-common.asl") }
