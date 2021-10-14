# Splunk Integration on Quarkus

## Setup

Set HTTP Event Collector (HEC) on your (local) Splunk and copy the token.
Ensure that the HEC has SSL disabled and that all tokens are enabled.
## Running

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
