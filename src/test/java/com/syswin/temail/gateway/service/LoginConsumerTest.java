package com.syswin.temail.gateway.service;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import au.com.dius.pact.consumer.ConsumerPactTestMk2;
import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.gson.Gson;
import com.syswin.temail.gateway.entity.Response;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class LoginConsumerTest extends ConsumerPactTestMk2 {

  private final String path = "/verify";
  private final Gson gson = new Gson();
  private final RestTemplate restTemplate = new RestTemplate();
  private final String sean = "sean@t.email";
  private final String signature = "signed-abc";
  private final String unsignedText = "abc";

  @Before
  public void setUp() {
    restTemplate.setErrorHandler(new SilentResponseErrorHandler());
  }

  @Override
  public RequestResponsePact createPact(PactDslWithProvider pactDslWithProvider) {
    Map<String, String> headers = new HashMap<>();
    headers.put(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE);

    return pactDslWithProvider
        .given("User sean is registered")
          .uponReceiving("Sean requested to log in")
          .method("POST")
          .body("{\n"
              + "  \"temail\": \"" + sean + "\",\n"
              + "  \"signature\": \"" + signature + "\",\n"
              + "  \"unsignedBytes\": \"" + unsignedText + "\"\n"
              + "}")
          .headers(headers)
          .path(path)
          .willRespondWith()
          .status(200)
          .headers(singletonMap(CONTENT_TYPE, APPLICATION_JSON_VALUE))
          .body(gson.toJson(Response.ok(OK, "Success")))
        .given("User jack is not registered")
          .uponReceiving("Jack requested to log in")
          .method("POST")
          .body("{\n"
              + "  \"temail\": \"jack@t.email\",\n"
              + "  \"signature\": \"" + signature + "\",\n"
              + "  \"unsignedBytes\": \"" + unsignedText + "\"\n"
              + "}")
          .headers(headers)
          .path(path)
          .willRespondWith()
          .status(403)
          .headers(singletonMap(CONTENT_TYPE, APPLICATION_JSON_VALUE))
          .body(gson.toJson(Response.failed(FORBIDDEN)))
        .given("User mike is registered, but server is out of work")
          .uponReceiving("Mike requested to log in")
          .method("POST")
          .body("{\n"
              + "  \"temail\": \"mike@t.email\",\n"
              + "  \"signature\": \"" + signature + "\",\n"
              + "  \"unsignedBytes\": \"" + unsignedText + "\"\n"
              + "}")
          .headers(headers)
          .path(path)
          .willRespondWith()
          .status(500)
          .headers(singletonMap(CONTENT_TYPE, APPLICATION_JSON_VALUE))
          .body(gson.toJson(Response.failed(INTERNAL_SERVER_ERROR)))
        .toPact();
  }

  @Override
  public void runTest(MockServer mockServer) {
    String url = mockServer.getUrl() + path;

    LoginService loginService = new LoginService(restTemplate, url);

    // login success scenario
    ResponseEntity<Response> responseEntity = loginService.login(sean, signature, unsignedText);

    assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
    assertThat(responseEntity.getBody().getCode()).isEqualTo(OK.value());
    assertThat(responseEntity.getBody().getData()).isEqualTo("Success");

    // login rejected scenario
    responseEntity = loginService.login("jack@t.email", signature, unsignedText);

    assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
    assertThat(responseEntity.getBody().getCode()).isEqualTo(FORBIDDEN.value());

    // server out of work scenario
    responseEntity = loginService.login("mike@t.email", signature, unsignedText);

    assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    assertThat(responseEntity.getBody().getCode()).isEqualTo(INTERNAL_SERVER_ERROR.value());

    LoginService unreachableService = new LoginService(restTemplate, "http://localhost:99");
    responseEntity = unreachableService.login(sean, signature, unsignedText);

    assertThat(responseEntity.getStatusCode()).isEqualTo(SERVICE_UNAVAILABLE);
    assertThat(responseEntity.getBody().getCode()).isEqualTo(SERVICE_UNAVAILABLE.value());
  }

  @Override
  protected String providerName() {
    return "temail-dispatcher";
  }

  @Override
  protected String consumerName() {
    return "temail-gateway";
  }

}