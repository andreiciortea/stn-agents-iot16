#!/bin/bash
echo Registerig devices: wristband, mattress cover, window curtains, and lights.

java -jar cf-client-1.1.0-SNAPSHOT.jar POST coap://127.0.0.1:5683/rd?ep=davids_wristband "</wristband>;rt=\"http://example.org#SmartWristband\";con=\"coap://127.0.0.1:${1:-60000}\""

java -jar cf-client-1.1.0-SNAPSHOT.jar POST coap://127.0.0.1:5683/rd?ep=davids_mattress_cover "</mattress_cover>;rt=\"http://example.org#SmartMattressCover\";con=\"coap://127.0.0.1:${1:-60000}\""

java -jar cf-client-1.1.0-SNAPSHOT.jar POST coap://127.0.0.1:5683/rd?ep=bedroom_window_curtains "</window_curtains>;rt=\"http://example.org#SmartWindowCurtains\";con=\"coap://127.0.0.1:${1:-60000}\""

java -jar cf-client-1.1.0-SNAPSHOT.jar POST coap://127.0.0.1:5683/rd?ep=bedroom_lights "</lights>;rt=\"http://example.org#SmartLights\";con=\"coap://127.0.0.1:${1:-60000}\""

java -jar cf-client-1.1.0-SNAPSHOT.jar POST coap://127.0.0.1:5683/rd?ep=outside_light_sensor "</outside_light_sensor>;rt=\"http://example.org#LightSensor\";con=\"coap://127.0.0.1:${1:-60000}\""
