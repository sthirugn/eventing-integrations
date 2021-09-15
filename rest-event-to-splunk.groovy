
// Listen on POST /event.
from("rest:post:event")
  // Log the message/data.
  .to("log:info")
  // Send message synchronously to Camel enpoint named "splunk".
  .to("direct:splunk")
  // Send a response.
  .transform().constant('{"status":"sent"}');


// Receive messages on internal enpoint (within the same JVM)
// named "splunk".
from('direct:splunk')
  // Remove headers of previous message,
  // specifically the ones that HTTP components use
  // to prevent passing the REST path to the HTTP producer.
  // Without this it would use path: /services/collector/raw/event
  // where the "/event" is the REST endpoint configured on previous
  // component.
  .removeHeaders("CamelHttp*")

  // Send the message to Splunk's HEC as raw data.
  // It sends token via Basic Preemptive Authentication.
  // POST method is being used, set up explicitly
  // (see https://camel.apache.org/components/latest/http-component.html#_which_http_method_will_be_used).
  .to('http://SPLUNKHOST:8088/services/collector/raw?' +
      'authenticationPreemptive=true&' +
      'authMethod=Basic&' +
      'httpMethod=POST&' +
      'authUsername=x&' +
      'authPassword=PASTETOKENHERE')
  // Log after a successful send.
  .to('log:info')
