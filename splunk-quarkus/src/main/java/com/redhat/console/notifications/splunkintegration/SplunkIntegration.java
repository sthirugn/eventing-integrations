/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.cloud.notifications.splunkintegration;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.Processor;

/**
 * The main class that does the work setting up the Camel routes.
 * Entry point for messages is below 'from(INCOMING_CHANNEL)'
 * Upon success/failure a message is returned to the RETURN_CHANNEL
 * topic.
 */

/*
 * We need to register some classes for reflection here, so that
 * native compilation can work if desired.
 */
@RegisterForReflection(targets = {
    Exception.class,
    IOException.class
})
@ApplicationScoped
public class SplunkIntegration extends EndpointRouteBuilder {

    // The name of our component. Must be unique
    public static final String COMPONENT_NAME = "splunk-integration";

    private static final Config CONFIG = ConfigProvider.getConfig();
    // Only accept/listen on these CloudEvent types
    public static final String CE_TYPE = "com.redhat.console.notification.toCamel." + COMPONENT_NAME;
    // Brokers
    public static final String KAFKA_INGRESS_BROKERS = CONFIG.getValue("kafka.ingress.brokers", String.class);
    // Event incoming Kafka topic
    public static final String KAFKA_INGRESS_TOPIC = CONFIG.getValue("kafka.ingress.topic", String.class);
    // Splunk Token
    private static final String SPLUNK_TOKEN = CONFIG.getValue("splunk.token", String.class);

    @Override
    public void configure() throws Exception {
        configureIngress();
        configureHandler();
    }

    private void configureIngress() throws Exception {
        from(kafka(KAFKA_INGRESS_TOPIC).brokers(KAFKA_INGRESS_BROKERS))
            // Decode CloudEvent
            .process(new CloudEventDecoder())
            // We check that this is our type.
            // Otherwise, we ignore the message there will be another component that takes care
            .filter().simple("${header.ce-type} == '" + CE_TYPE + "'")
                // Log the parsed cloudevent message.
                .to(log("info"))
                .to(direct("handler"))
            .end();
    }

    private void configureHandler() throws Exception {
        // Receive messages on internal enpoint (within the same JVM)
        // named "splunk".
        from(direct("handler"))
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
            .to(http("{{splunk.host}}/services/collector/raw")
                .authenticationPreemptive(true)
                .authMethod("Basic")
                .httpMethod("POST")
                .authUsername("x")
                .authPassword(SPLUNK_TOKEN))
            // Log after a successful send.
            .to(log("info"));
    }
}
