// Agent outside_light_sensor in project swot_agents

/* Initial beliefs and rules */

owner(david).

outside_light_level(120).

{ include("inc/config.asl") }


/* Initial goals */

!start.

/* Plans */

+!start : true <-
    !setup;
    !createDeviceArtifact.


// Create a TI SensorTag artifact (if used).
// Currently supports only communication via an MQTT broker.
+!createDeviceArtifact : use_ti_sensortag(true)
    & ti_sensortag_mqtt_broker_address(BrokerAddress)
    & ti_mqtt_client_id(ClientId)
    & ti_mqtt_publish_topic(Topic)
    & ti_mqtt_username(Username)
    & ti_mqtt_password(Password) <-
        makeArtifact("sensortag", "fr.emse.ci.stn.aacontainer.artifacts.devices.SensorTagArtifact", 
            [BrokerAddress, ClientId, Topic, Username, Password], _);
        focusWhenAvailable("sensortag").

// No device, just simulate the readings.
+!createDeviceArtifact : true <-
    .wait(5000);
    .print("No sensors, simulating sensor readings...");
    !simulateLightLevel.

// Artifact was created previously, fail gracefully.
-!createDeviceArtifact : true .


+!simulateLightLevel : outside_light_level(120) <-
    -+outside_light_level(80);
    .wait(10000);
    !simulateLightLevel.

+!simulateLightLevel : true <-
    -+outside_light_level(120);
    .wait(10000);
    !simulateLightLevel.

// Post sensor readings to the STN.
+outside_light_level(Level) : use_ti_sensortag(true) <-
//    .print("Light level: ", Level);
    !publishSensorReading(Level).

+outside_light_level(Level) : true <-
    .print("Light level (simulated): ", Level);
    !publishSensorReading(Level).

+!publishSensorReading(Level) : my_account_uri(MyAccountUri) <- 
    .concat("tell outside_light_level(", Level, ")", Message);
    publishMessage(MyAccountUri, Message).

+!publishSensorReading(_) : true .

{ include("inc/stn-common.asl") }
