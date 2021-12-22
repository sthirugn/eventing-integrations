# Splunk Integration on Quarkus

## Development/Local

### Prerequisites

* podman/docker
* [OpenJDK Development Environment](https://openjdk.java.net/guide/)
  (`java-latest-openjdk-devel` package in Fedora)

### Setup

Set HTTP Event Collector (HEC) on your (local) Splunk and copy the token.
Ensure that the HEC has SSL disabled and that all tokens are enabled.

### Container Image Build

```
$ cd splunk-quarkus
$ podman build -f Dockerfile.jvm -t quay.io/vkrizan/eventing-splunk-quarkus ..
```

### Running

Running within container:

```
podman run -it -e ACG_CONFIG=/cdapp/devel.json -v devel.json:/cdapp/devel.json quay.io/vkrizan/eventing-splunk-quarkus
```

You might ommit the interative terminal options `-it` if you want to have
it running in background (detached).


To run it locally with dev mode execute:

```
$ ../mvnw quarkus:dev -Dquarkus.kafka.devservices.enabled=false
```

The integration would connect to Kafka on `platform.notifications.tocamel`
and would pass messages to configured Splunk.

### Trying it out

Generate a message on `platform.notifications.tocamel` Kafka topic.

Manually this can be achieved for example by producing a message using
[`kafka-console-producer.sh`](https://kafka.apache.org/quickstart)
tool from Kafka:
```
kafka-console-producer.sh --bootstrap-server localhost:9092 --topic platform.notifications.tocamel
```

Here is an example CloudEvent for testing:
```
{"data":"{\"notif-metadata\":{\"extras\":\"{\\\"token\\\":\\\"TOKEN\\\"}\",\"url\":\"localhost:8088\"},\"payload\":\"{}\"}","type":"com.redhat.console.notification.toCamel.splunk"}
```
(don't forget to replace `TOKEN` with your HEC token)

Within platform this can be achieved for example using the Drift service:
* registering a system
* creating a baseline out of the registered system
* assigning the system to baseline
* chaning a fact of the baseline (e.g. bios) or changing the system
  (updating a package)
* running a system check-in from the system

## Deployment/Ephemeral Environment

### Prerequisites

* oc/kubectl
* Logged into an Ephemeral Cluster
* [Bonfire](https://github.com/RedHatInsights/bonfire)

You might check out [**Drift Clowder Documentation**](https://docs.google.com/document/d/1As5TC4WHTrflrt4dt9rRsfAhWsQD_94yNYCy-ucLc0c/edit)
for a step by step guide.
You can follow it up to the "Deploying a custom image", using
bonfire config listed below.

### Preparation

Reserve a namespace (unless you are running a local minikube):
```
$ bonfire namespace reserve
```

and set default project/namespace
```
$ oc project ephemeral-N
```
where N is the number of the reserved ephemeral namespace.


Allow always pulling latest images in the environment/namespace:
```
$ oc patch env env-ephemeral-N --type='json' -p='[{"op": "replace", "path": "/spec/providers/deployment/omitPullPolicy", "value":true}]'
```
where N is the number of the reserved ephemeral namespace.

### Process and Deploy

```
$ oc process -f ./clowdapp.yaml -o yaml \
  -p IMAGE_TAG=latest \
  -p ENV_NAME=env-ephemeral-NN | oc apply -f -
```
replace NN with the number of the reserved ephemeral namespace.


(Note that this can be done using bonfire with a local config.)
