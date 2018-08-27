package com.syswin.temail.gateway.status;

import static java.util.Collections.singletonMap;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import au.com.dius.pact.consumer.ConsumerPactTestMk2;
import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.gson.Gson;
import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.gateway.entity.TemailSocketInfo;
import com.syswin.temail.gateway.entity.TemailSocketInstance;
import com.syswin.temail.gateway.service.RemoteStatusService;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.reactive.function.client.WebClient;

public class DiscoveryConsumerTest extends ConsumerPactTestMk2 {

  private final Gson gson = new Gson();
  private final String path = "/locations";
  private final String temail = "sean@t.email";
  private final String deviceId = "iOS-sean";
  private final String mqTopic = "temail-gateway";
  private final String mqTag = "gateway-localhost";
  private final String gatewayHost = "localhost";
  private final String processId = "12345";

  private final TemailGatewayProperties properties = new TemailGatewayProperties();

  @Override
  public RequestResponsePact createPact(PactDslWithProvider pactDslWithProvider) {
    Map<String, String> headers = new HashMap<>();
    headers.put(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE);

    return pactDslWithProvider
        .given("New connection")
        .uponReceiving("new connection established")
        .method("POST")
        .body(gson.toJson(location()))
        .headers(headers)
        .path(path)
        .willRespondWith()
        .status(201)
        .headers(singletonMap(CONTENT_TYPE, APPLICATION_JSON_VALUE))
        .body(gson.toJson(Response.ok(CREATED, "Success")))
        .toPact();
  }

  @Override
  public void runTest(MockServer mockServer) {
    String url = mockServer.getUrl() + path;
    properties.setUpdateSocketStatusUrl(url);
    properties.setHostOf(gatewayHost);
    properties.setProcessId(processId);
    properties.setMqTopic(mqTopic);
    properties.setMqTag(mqTag);

    RemoteStatusService statusService = new RemoteStatusService(properties, WebClient.create());
    statusService.addSession(temail, deviceId).block();
  }

  @Override
  protected String providerName() {
    return "temail-discovery";
  }

  @Override
  protected String consumerName() {
    return "temail-gateway";
  }

  @NotNull
  private TemailSocketInfo location() {
    TemailSocketInstance status = new TemailSocketInstance(
        deviceId,
        gatewayHost,
        processId,
        mqTopic,
        mqTag);

    return new TemailSocketInfo(temail, "add", status);
  }
}
