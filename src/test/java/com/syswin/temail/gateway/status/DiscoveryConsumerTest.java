package com.syswin.temail.gateway.status;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import au.com.dius.pact.consumer.ConsumerPactTestMk2;
import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.gson.Gson;
import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.gateway.entity.TemailAccoutLocation;
import com.syswin.temail.gateway.entity.TemailAccoutLocations;
import com.syswin.temail.gateway.service.RemoteStatusService;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
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
  private final Queue<Response<Void>> responses = new LinkedList<>();

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
          .body(gson.toJson(Response.ok(CREATED)))
        .given("Remove connection")
          .uponReceiving("remove existing connection")
          .method("PUT")
          .body(gson.toJson(location()))
          .headers(headers)
          .path(path)
          .willRespondWith()
          .status(200)
          .headers(singletonMap(CONTENT_TYPE, APPLICATION_JSON_VALUE))
          .body(gson.toJson(Response.ok()))
        .toPact();
  }

  @Override
  public void runTest(MockServer mockServer) {
    String url = mockServer.getUrl() + path;
    properties.setUpdateSocketStatusUrl(url);
    properties.getInstance().setHostOf(gatewayHost);
    properties.getInstance().setProcessId(processId);
    properties.getRocketmq().setMqTopic(mqTopic);
    properties.getInstance().setMqTag(mqTag);

    RemoteStatusService statusService = new RemoteStatusService(properties, WebClient.create());
    statusService.addSession(temail, deviceId, responses::add);
    waitAtMost(5, SECONDS).until(() -> !responses.isEmpty());
    assertThat(responses.poll().getCode()).isEqualTo(CREATED.value());

    statusService.removeSession(temail, deviceId, responses::add);
    waitAtMost(5, SECONDS).until(() -> !responses.isEmpty());
    assertThat(responses.poll().getCode()).isEqualTo(OK.value());
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
  private TemailAccoutLocations location() {
    TemailAccoutLocation status = new TemailAccoutLocation(
        temail,
        deviceId,
        gatewayHost,
        processId,
        mqTopic,
        mqTag);
    return new TemailAccoutLocations(singletonList(status));
  }
}
