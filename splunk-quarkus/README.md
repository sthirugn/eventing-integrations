# Splunk Integration on Quarkus

## Prerequisites

* podman/docker
* [OpenJDK Development Environment](https://openjdk.java.net/guide/)
  (`java-latest-openjdk-devel` package in Fedora)

## Setup

Set HTTP Event Collector (HEC) on your (local) Splunk and copy the token.
Ensure that the HEC has SSL disabled and that all tokens are enabled.
## Container Image Build

```
$ cd integrations/splunk-quarkus
$ podman build -f Dockerfile.jvm -t splunk-quarkus ..
```

## Running

Running within container:

```
podman run -it -p 8080:8080 -e SPLUNK_HOSTNAME=SPLUNKIP -e SPLUNK_TOKEN=token splunk-quarkus
```

You might ommit the interative terminal options `-it` if you want to have
it running in background (detached).


To run it locally with dev mode replace `SPLUNKIP` and `TOKEN` and execute:

```
$ ../mvnw quarkus:dev -Dsplunk.host=SPLUNKIP:8088 -Dsplunk.token=TOKEN
```

The REST endpoint created by the Integration is
`POST /event` bound on `localhost:8080`.

## Trying it out

Send an event with POST using curl:

```
$ curl http://localhost:8080/event \
    -H "Content-Type: application/json" \
    -d '{"event": "Test"}'
```

Pay attention to the `Content-Type` which needs to be set for Camel
to recognize the message body.
