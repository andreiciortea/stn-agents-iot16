#!/bin/bash

echo Deregistering devices..

java -jar cf-client-1.1.0-SNAPSHOT.jar DELETE coap://127.0.0.1:5683/rd/davids_wristband

java -jar cf-client-1.1.0-SNAPSHOT.jar DELETE coap://127.0.0.1:5683/rd/davids_mattress_cover

java -jar cf-client-1.1.0-SNAPSHOT.jar DELETE coap://127.0.0.1:5683/rd/bedroom_window_curtains

java -jar cf-client-1.1.0-SNAPSHOT.jar DELETE coap://127.0.0.1:5683/rd/bedroom_lights
