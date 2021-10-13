# Splunk Integration on Quarkus

## Running

Before running, please update the file with the HEC token by replacing
`PASTETOKENHERE` and setting Splunk's hostname by replacing `SPLUNKHOST`!

```
$ ../mvnw quarkus:dev
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
