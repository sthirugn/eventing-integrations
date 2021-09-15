# REST to Splunk

Integration that creates a REST endpoint and resends it to
Splunk's HTTP Event Collector (HEC).

## Prerequisites

* Kubernetes/OpenShift cluster
* CamelK operator installed (`kamel install`)
* HTTP Event Collector set up in Splunk
  * Settings > Add Data > monitor > HTTP Event Collector
  * [Splunk HEC Documentation](https://docs.splunk.com/Documentation/Splunk/8.2.2/Data/UsetheHTTPEventCollector)

## Running

Before running, please update the file with the HEC token by replacing
`PASTETOKENHERE` and setting Splunk's hostname by replacing `SPLUNKHOST`!

```
$ kamel run rest-event-to-splunk.groovy [--dev]
```

The REST endpoint created by the Integration is
`POST /event`.

## Trying it out

Set up port forwarding from the cluster:

```
oc port-forward service/rest-event-to-splunk 8111:80
```

Send an event with POST using curl:

```
$ curl http://localhost:8111/event \
    -H "Content-Type: application/json" \
    -d '{"event": "Test"}'
```

Pay attention to the `Content-Type` which needs to be set for Camel
to recognize the message body.
