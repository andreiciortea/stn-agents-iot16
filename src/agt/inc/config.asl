/* Configuration settings */

stn_platform_http_uri("http://localhost:8080").
stn_platform_websocket_uri("ws://localhost:8090").

resource_directory_address("coap://localhost:5683").

use_philips_hue(false).
philips_hue_light_uri("http://192.168.0.100/api/bhBWRz2DWOEqjSwR9cmDBeZgVfV3pWvjrdYdpocw/lights/1/state").

use_ti_sensortag(false).
ti_sensortag_mqtt_broker_address("tcp://broker.hivemq.com:1883").
ti_mqtt_client_id("fr.emse.ci.iot16.demo").
ti_mqtt_publish_topic("emse-ci").
ti_mqtt_username("").
ti_mqtt_password("").
