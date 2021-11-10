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
$ cd integrations/splunk-quarkus
$ podman build -f Dockerfile.jvm -t quay.io/vkrizan/eventing-splunk-quarkus ..
```

### Running

Running within container:

```
podman run -it -e SPLUNK_HOSTNAME=SPLUNKIP -e SPLUNK_TOKEN=token\
 -p KAFKA_INGRESS_BROKERS=BROKER:9092 quay.io/vkrizan/eventing-splunk-quarkus
```

You might ommit the interative terminal options `-it` if you want to have
it running in background (detached).


To run it locally with dev mode replace `SPLUNKIP`, `TOKEN` and `BROKER`
and execute:

```
$ ../mvnw quarkus:dev -Dsplunk.host=SPLUNKIP:8088 -Dsplunk.token=TOKEN \
  -Dkafka.ingress.brokers=BROKER:9092
```

The integration would connect to Kafka on `platform.notifications.tocamel`
and would pass messages to configured Splunk.

### Trying it out

Generate a message on `platform.notifications.tocamel` Kafka topic.

This can be done for example using the Drift service:
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


### Create Secret

```
$ oc create secret generic eventing-splunk-credentials \
 --from-literal=hostname=SPULNKIP \
 --from-literal=port=8088 \
 --from-literal=token=TOKEN
```

replace `SPULNKIP` and `TOKEN`.

### Process and Deploy

```
$ oc process -f ./clowdapp.yaml -o yaml \
  -p IMAGE_TAG=kafka\
  -p ENV_NAME=env-ephemeral-NN\
  -p KAFKA_INGRESS_BROKERS=BROKER:9092 | oc apply -f -
```
replace NN with the number of the reserved ephemeral namespace,
and `BROKER` with the hostname of the ephemeral broker instance
(e.g. `env-ephemeral-42-76676c3d-kafka-brokers`).


(Note that this can be done using bonfire with a local config.)


### Cleanup

```
$ oc delete clowdapp eventing-splunk-quarkus
$ oc delete secret eventing-splunk-credentials
```
