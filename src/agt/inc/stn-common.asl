
localhost_uri("http://localhost:58880").

/* Set up STN artifacts */

+!setup : .my_name(N) & localhost_uri(LocalUri) <-
    .concat("/", N, "/", RelativeCallbackUri);
    .concat(LocalUri, RelativeCallbackUri, CallbackUri);
//    .print("My callback URI is: ", CallbackUri);
    +callback_uri(CallbackUri);
    .concat(CallbackUri, "#thing", WebId);
    +webid(WebId);
    // TODO: remove after full websocket support
//    ?current_wsp(_,WspName,_);
//    .print("Wsp Name: ", WspName);
//    .print("Going for the dispatcher");
    focusWhenAvailable("dispatcher");
    focusWhenAvailable("WSdispatcher");
    register(RelativeCallbackUri);
    registerToWS(CallbackUri);
    focusWhenAvailable("interpreter");
    !registerToSTN.
//    .print("READY TO GO !!!").

/* Register to the STN. */

+!registerToSTN : owner_account_uri(OwnersAccountUri) & callback_uri(CallbackUri) & webid(WebId) <-
//+owner_account_uri(OwnersAccountUri) : callback_uri(CallbackUri) & webid(WebId) <-
//    .print("Received David's account URI: ", OwnersAccountUri);
//    !register.
//+!register : owner_account_uri(OwnersAccountUri) & callback_uri(CallbackUri) & webid(WebId) <-
//    .print("Creating user account...");
    registerToSTN(WebId, CallbackUri, OwnersAccountUri, AccountUri);
    +my_account_uri(AccountUri);
//    .print("My account URI: ", AccountUri);
    // Connect to other agents owned by the same owner.
    !connectToAgents.
//    .print("Creating relation to David...");
//    !connectTo(OwnersAccountUri).

// Setup was not yet completed, wait 500ms and retry.
+!registerToSTN : true <-
    .wait(500);
    !registerToSTN.

//+!connectTo(Target) : true <- .print("Connection target: ", Target).

/* Connect to all agents following the owner */

+!connectToAgents : owner_account_uri(OwnersAccountUri) <-
    .print("Connecting to all other agents owned by David...");
    getThingsOwnedBy(OwnersAccountUri, Things);
    .length(Things, L);
//    .print("Found #", L - 1, " agents!");
    !connectToAgents(Things).

+!connectToAgents([]) : true . // <- .print("Connections created!").

+!connectToAgents([AccountUri | T]) : my_account_uri(AccountUri) <-
    !connectToAgents(T).

+!connectToAgents([H | T]) : true <-
    !connectTo(H);
    !connectToAgents(T).

+!connectTo(Target) : my_account_uri(AccountUri) & connectedTo(_, MyAccountUri, Target) .

@create_connection[atomic]
+!connectTo(Target) : my_account_uri(AccountUri) <-
//    .print("Connecting to: ", Target);
    connectTo(AccountUri, Target, RelationUri);
    +connectedTo(RelationUri, AccountUri, Target).


/* Interpret new notifications. */

+notification(Message) : true <-
//    .print("Notification: ", Message);
    interpretNotification(Message).


/* Automatically follow a new follower. */

+newFollower(RelationUri, FollowerUri, MyAccountUri) : my_account_uri(MyAccountUri) <-
    +connectedTo(RelationUri, FollowerUri, MyAccountUri);
//    .print("Got a new follower!");
    !connectTo(FollowerUri).
    
//    !checkNewFollower(FollowerUri).

//+!checkNewFollower(FollowerUri) : my_account_uri(MyAccountUri) <-
//    ?connectedTo(_, MyAccountUri, FollowerUri).

//-!checkNewFollower(FollowerUri) : my_account_uri(MyAccountUri) <-
//    !connectTo(FollowerUri).

+newMessage(SenderUri, Performative, Content) : true <-
//    .print("Got message from ", SenderUri, ": ", Performative, " ", Content);
//    .term2string(Content, Term);
    jia.string2term(Content, Term);
//    .print("Got term: ", Term);
    !processMessage(SenderUri, tell, Term).

/* Disregard irrelevant messages */

+!processMessage(_,_,_) : true .
