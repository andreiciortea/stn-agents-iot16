#!/bin/bash

echo Deregistering the wristband..

java -jar cf-client-1.1.0-SNAPSHOT.jar DELETE coap://127.0.0.1:5683/rd/davids_wristband
