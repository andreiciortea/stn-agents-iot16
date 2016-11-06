#!/bin/bash

echo Registering wristband..

java -jar cf-client-1.1.0-SNAPSHOT.jar POST coap://127.0.0.1:5683/rd?ep=davids_wristband "</wristband>;rt=\"http://example.org#SmartWristband\";con=\"coap://127.0.0.1:${1:-60000}\""
