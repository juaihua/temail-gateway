package com.syswin.temail.gateway.service;

import static com.syswin.temail.gateway.client.PacketMaker.loginPacket;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import au.com.dius.pact.consumer.ConsumerPactTestMk2;
import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.gson.Gson;
import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.packet.SimplePacketUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author 姚华成
 * @date 2018-11-02
 */
public abstract class AbstractAuthServiceConsumerTest extends ConsumerPactTestMk2 {

  protected final String path = "/verify";
  protected final Gson gson = new Gson();
  protected CDTPPacket normalPacket;
  protected CDTPPacket notRegPacket;
  protected CDTPPacket signErrorPacket;

  protected volatile Response resultResponse = null;
  protected volatile Response errorResponse = null;

  @Override
  public RequestResponsePact createPact(PactDslWithProvider pactDslWithProvider) {
    Map<String, String> headers = new HashMap<>();
    headers.put(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE);

    String deviceId = "deviceId";
    CDTPPacket packet = loginPacket("", deviceId);
    CDTPHeader header = packet.getHeader();
    header.setSender("sean@t.email");
    normalPacket = new CDTPPacket(packet);
    header.setSender("jack@t.email");
    notRegPacket = new CDTPPacket(packet);
    header.setSender("mike@t.email");
    signErrorPacket = new CDTPPacket(packet);

    return pactDslWithProvider
        .given("User sean is registered")
        .uponReceiving("Sean requested to log in")
        .path(path)
        .method("POST")
        .headers(headers)
        .body(gson.toJson(SimplePacketUtil.INSTANCE.toTrans(normalPacket)))
        .willRespondWith()
        .status(OK.value())
        .headers(singletonMap(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE))
        .body(gson.toJson(Response.ok(OK, "Success")))
        .given("User jack is not registered")
        .uponReceiving("Jack requested to log in")
        .path(path)
        .method("POST")
        .headers(headers)
        .body(gson.toJson(SimplePacketUtil.INSTANCE.toTrans(notRegPacket)))
        .willRespondWith()
        .status(NOT_FOUND.value())
        .headers(singletonMap(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE))
        .body(gson.toJson(Response.failed(NOT_FOUND)))
        .given("User mike is registered, but server is out of work")
        .uponReceiving("Mike requested to log in")
        .path(path)
        .method("POST")
        .headers(headers)
        .body(gson.toJson(SimplePacketUtil.INSTANCE.toTrans(signErrorPacket)))
        .willRespondWith()
        .status(BAD_REQUEST.value())
        .headers(singletonMap(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE))
        .body(gson.toJson(Response.failed(BAD_REQUEST)))
        .toPact();
  }

  @Override
  public void runTest(MockServer mockServer) {
    String url = mockServer.getUrl() + path;

    SuccessConsumer successConsumer = new SuccessConsumer();
    FailedConsumer failedConsumer = new FailedConsumer();

    AuthService authService = getAuthService(url);
    authService.validSignature(normalPacket, successConsumer, failedConsumer);
    waitAtMost(3, SECONDS).until(() -> resultResponse != null);
    assertThat(resultResponse.getCode()).isEqualTo(OK.value());

    authService.validSignature(notRegPacket, successConsumer, failedConsumer);
    waitAtMost(3, SECONDS).until(() -> errorResponse != null);
    assertThat(errorResponse.getCode()).isEqualTo(NOT_FOUND.value());

    authService.validSignature(signErrorPacket, successConsumer, failedConsumer);
    waitAtMost(3, SECONDS).until(() -> errorResponse != null);
    assertThat(errorResponse.getCode()).isEqualTo(BAD_REQUEST.value());
  }

  protected abstract AuthService getAuthService(String url);

  @Override
  protected String providerName() {
    return "temail-dispatcher";
  }

  @Override
  protected String consumerName() {
    return "temail-gateway";
  }


  protected class SuccessConsumer implements Consumer<Response> {

    @Override
    public void accept(Response response) {
      resultResponse = response;
      errorResponse = null;
    }
  }

  protected class FailedConsumer implements Consumer<Response> {

    @Override
    public void accept(Response response) {
      resultResponse = null;
      errorResponse = response;
    }
  }
}
