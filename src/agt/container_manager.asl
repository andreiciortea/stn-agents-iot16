// Agent container_manager in project swot_agents

/* Initial beliefs and rules */

{ include("inc/config.asl") }


/* Initial goals */

!start.


/* Plans */

+!start : true <-
    .print("Hello world! Setting up workspace...");
    // Set up artifacts required to interact with the STN platform
    !setUpDispatcher;
    !setUpClient;
    // Register David to set up the scenario
    !registerDavid;
    // Start sync with the CoRE resource directory
    !manageAgents.


/* Workspace setup */

+!setUpDispatcher : stn_platform_websocket_uri(STNWebsocketUri) <-
    // TODO: use a single dispatcher
    makeArtifact("dispatcher", "fr.emse.ci.stn.aacontainer.artifacts.notifications.ApacheNotificationDispatcher", [58880], Dispatcher);
    focusWhenAvailable("dispatcher");
    makeArtifact("WSdispatcher", "fr.emse.ci.stn.aacontainer.artifacts.notifications.WebSocketNotificationDispatcher", [STNWebsocketUri], WebSocketDispatcher);
    focusWhenAvailable("WSdispatcher");
    makeArtifact("interpreter", "fr.emse.ci.stn.aacontainer.artifacts.notifications.NotificationInterpreter", [], Interpreter);
    focusWhenAvailable("interpreter");
    start[artifact_id(Dispatcher)];
    start[artifact_id(WebSocketDispatcher)].

+!setUpClient : stn_platform_http_uri(STNPlatformUri) <-
    makeArtifact("client", "fr.emse.ci.stn.aacontainer.artifacts.STNClient", [STNPlatformUri], Client).


/* Scenario setup */

// Register a user account for David. The URI of David's user account is then given to
// the agents in our scenario to be used as an entry point into the STN.
+!registerDavid : true <-
    registerToSTN("", "", "", AccountUri);
    +owner_account_uri(AccountUri);
    .print("Broadcasting the URI of David's user account...");
    .broadcast(tell, owner_account_uri(AccountUri)).


// The container manager synchronizes with a CoAP Resource Directory (RD) to create / kill agents
// such that they reflect resources currently available in the IoT environment. 
+!manageAgents : resource_directory_address(RdAddress) <-
    // Create an artifact to access and use a CoAP RD.
    makeArtifact("directory", "fr.emse.ci.stn.aacontainer.artifacts.coap.ResourceDirectory", [RdAddress], CfRD);
    !syncRD.


/* Cf-RD sync logic */

+!syncRD : true <-
    // Retrieve a list of resources registered with the CoAP RD.
    getResources(Resources);
    // Process the list and update the set of active agents accordingly.
    !updateAgentBase(Resources);
    // Sync every 5s with the CoAP RD.
    .wait(2000);
    !syncRD.

// If the list is empty, check for any agents created previously that are out-of-sync.
+!updateAgentBase([]) : true <-
    .findall(X, alive(X), CreatedAgents);
    .findall(Y, seen(Y), SeenAgents);
    .difference(CreatedAgents, SeenAgents, Diff);
    .abolish(seen(_));
    !killUnseen(Diff).


// Kill all out-of-sync agents.
+!killUnseen([]) : true .

+!killUnseen([A | T]) : true <-
    .print("Killng ", A, " agent");
    .kill_agent(A);
    -alive(A);
    !killUnseen(T).


// Create an agent for a discovered resource of a known type.
+!createAgent(Agent, AslFile, DeviceUri) : owner_account_uri(AccountUri) <-
    .print("Creating agent ", Agent);
    .create_agent(Agent, AslFile, [agentArchClass("c4jason.CAgentArch")]);
    +alive(Agent);
    +seen(Agent);
    // Send David's user account URI to the created agent
    .send(Agent, tell, owner_account_uri(AccountUri));
    // Send the device URI to the created agent
    .send(Agent, tell, device_uri(DeviceUri)).


// Handle resources of known types.

+!updateAgentBase([ [_, "http://example.org#SmartWristband"] | Tail]) : alive(wristband) <-
    +seen(wristband);
    !updateAgentBase(Tail).

+!updateAgentBase([ [DeviceUri, "http://example.org#SmartWristband"] | Tail]) : true <-
    !createAgent(wristband, "wristband.asl", DeviceUri);
    !updateAgentBase(Tail).


+!updateAgentBase([ [_, "http://example.org#SmartWindowCurtains"] | Tail]) : alive(window_curtains) <-
    +seen(window_curtains);
    !updateAgentBase(Tail).

+!updateAgentBase([ [DeviceUri, "http://example.org#SmartWindowCurtains"] | Tail]) : true <-
    !createAgent(window_curtains, "window_curtains.asl", DeviceUri);
    !updateAgentBase(Tail).


+!updateAgentBase([ [_, "http://example.org#SmartMattressCover"] | Tail]) : alive(mattress_cover) <-
    +seen(mattress_cover);
    !updateAgentBase(Tail).

+!updateAgentBase([ [DeviceUri, "http://example.org#SmartMattressCover"] | Tail]) : true <-
    !createAgent(mattress_cover, "mattress_cover.asl", DeviceUri);
    !updateAgentBase(Tail).


+!updateAgentBase([ [_, "http://example.org#SmartLights"] | Tail]) : alive(lights) <-
    +seen(lights);
    !updateAgentBase(Tail).

+!updateAgentBase([ [DeviceUri, "http://example.org#SmartLights"] | Tail]) : true <-
    !createAgent(lights, "lights.asl", DeviceUri);
    !updateAgentBase(Tail).
    
+!updateAgentBase([ [_, "http://example.org#LightSensor"] | Tail]) : alive(outside_light_sensor) <-
    +seen(outside_light_sensor);
    !updateAgentBase(Tail).

+!updateAgentBase([ [DeviceUri, "http://example.org#LightSensor"] | Tail]) : true <-
    !createAgent(outside_light_sensor, "outside_light_sensor.asl", DeviceUri);
    !updateAgentBase(Tail).


// Ignore resources of unknown types

+!updateAgentBase([ [Uri, Rt] | Tail]) : true <- !updateAgentBase(Tail).

