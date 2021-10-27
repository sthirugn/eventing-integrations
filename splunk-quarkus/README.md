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
podman run -it -p 8080:8080 -e SPLUNK_HOSTNAME=SPLUNKIP -e SPLUNK_TOKEN=token quay.io/vkrizan/eventing-splunk-quarkus
```

You might ommit the interative terminal options `-it` if you want to have
it running in background (detached).


To run it locally with dev mode replace `SPLUNKIP` and `TOKEN` and execute:

```
$ ../mvnw quarkus:dev -Dsplunk.host=SPLUNKIP:8088 -Dsplunk.token=TOKEN
```

The REST endpoint created by the Integration is
`POST /event` bound on `localhost:8080`.

### Trying it out

Send an event with POST using curl:

```
$ curl http://localhost:8080/event \
    -H "Content-Type: application/json" \
    -d '{"event": "Test"}'
```

Pay attention to the `Content-Type` which needs to be set for Camel
to recognize the message body.

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
$ oc process -f ./clowdapp.yaml -o yaml -p ENV_NAME=env-ephemeral-N | oc apply -f -
```
replace N with the number of the reserved ephemeral namespace.


(Note that this can be done using bonfire with a local config.)


### Trying out with port-forward

First port-forward the service:

```
$ oc port-forward service/eventing-splunk-quarkus-service 8080:9000
```

then follow the [Trying it out](#trying-it-out) steps.


### Cleanup

```
$ oc delete clowdapp eventing-splunk-quarkus
```
