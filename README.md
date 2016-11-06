# STN Agents (IoT16)

An Agents & Artifacts (A&A) container for deploying socio-technical networks in constrained RESTful environments. It also includes an implementation of the application scenario described in [1].

# Prerequisites

This project is built on top of JaCaMo v0.5.
1. Download and configure the [JaCaMo platform](http://jacamo.sourceforge.net/) version 0.5 (!).
2. Make sure the JACAMO_HOME variable is set. You can do so using: `export JACAMO_HOME=<...>`.

# Note

Both JaCaMo and the STN platform are undergoing major changes. To run this project, you should stick to JaCaMo v0.5 for now. For the STN platform, you should use the version provided in the `scripts/` folder. 

# How to run the demo application

1. Start the STN platform: `java -jar scripts/swot-hub-0.0.1-SNAPSHOT-fat.jar -conf scripts/config.json`

2. Start the CoRE Resource Directory. See Cf-RD in the [Californium tools repository](https://github.com/eclipse/californium.tools/).

3. Start the CoAP emulator: `java -jar scripts/coapemulator-0.0.1-SNAPSHOT-jar-with-dependencies.jar`

4. Start the A&A container via the JaCaMo Eclipse plugin or using the provided `jacamo` script: `./scripts/jacamo swot_agents.jcm`

5. Register devices to the CoRE RD: `./scripts/rd-register-devices.sh`

6. To deregister / register the wristband:

```
./scripts/rd-deregister-wristband.sh
./scripts/rd-register-wristband.sh
```

# Philips Hue and TI SensorTag

You can run the demo application with Philips Hue and TI SensorTag. See `src/agt/inc/config.asl` for configs.

# Resources

Feel free to play around with the A&A container. Some pointers to get you started:
- Rafael H Bordini, Jomi Fred Hubner, and Michael Wooldridge. Programming multi-agent systems in AgentSpeak using Jason, volume 8. John Wiley & Sons, 2007.
- [Multi-Agent Programming Course](http://www.emse.fr/~boissier/enseignement/maop16/) 


---

[1] Andrei Ciortea, Olivier Boissier, Antoine Zimmermann, and Adina Magda Florea. Responsive decentralized composition of service mashups for the internet of things. In Proceedings of the 6th International Conference on the Internet of Things (IoT). ACM, 2016.
