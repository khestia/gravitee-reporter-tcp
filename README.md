[![Build Status](https://ci.gravitee.io/buildStatus/icon?job=gravitee-io/gravitee-reporter-tcp)](https://ci.gravitee.io/job/gravitee-io/job/gravitee-reporter-tcp/job/master/)

# gravitee-reporter-tcp

Report GraviteeIO Gateway events to TCP listening server.


## Build

This plugin require :  

* Maven 3
* JDK 8

Once built, a plugin archive file is generated in : target/gravitee-reporter-tcp-1.0.0-SNAPSHOT.zip


## Deploy

Just unzip the plugin archive in your gravitee plugin workspace ( default is : ${node.home}/plugins )


## Configuration 

The configuration is loaded from the common GraviteeIO Gateway configuration file (gravitee.yml)

```YAML
reporters:
  tcp:
```

## Test locally

You can deploy the reporter and run the following command in your terminal (assuming you are using the default configuration) to display the GraviteeIO Gateway events:

`nc -4 -k -l -v localhost 8123`


