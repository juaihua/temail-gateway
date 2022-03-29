package com.syswin.temail.gateway.service;

import org.junit.Ignore;

@Ignore
public abstract class AbstractAuthServiceConsumerTest {

  //@Rule
  //public final PactProviderRuleMk2 mockTestProvider = new PactProviderRuleMk2("temail-dispatcher", this);
  //private final PacketEncoder packetEncoder = new PacketEncoder();
  //
  //protected final String path = "/verify";
  //protected final Gson gson = new Gson();
  //protected static CDTPPacket normalPacket;
  //protected static CDTPPacket notRegPacket;
  //protected static CDTPPacket signErrorPacket;
  //
  //protected final Queue<Response> resultResponses = new ArrayBlockingQueue<>(1);
  //protected final Queue<Response> errorResponses = new ArrayBlockingQueue<>(1);
  //
  //@BeforeClass
  //public static void setUp() {
  //  CDTPPacket packet = loginPacket("", "deviceId");
  //  CDTPHeader header = packet.getHeader();
  //  header.setSender("sean@t.email");
  //  normalPacket = new CDTPPacket(packet);
  //  header.setSender("jack@t.email");
  //  notRegPacket = new CDTPPacket(packet);
  //  header.setSender("mike@t.email");
  //  signErrorPacket = new CDTPPacket(packet);
  //}
  //
  //@Pact(consumer = "temail-gateway", provider = "temail-dispatcher")
  //public RequestResponsePact successFragment(PactDslWithProvider pactDslWithProvider) {
  //  Map<String, String> headers = new HashMap<>();
  //  headers.put(CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE);
  //
  //  return pactDslWithProvider
  //      .given("User sean is registered")
  //      .uponReceiving("Sean requested to log in")
  //      .path(path)
  //      .method("POST")
  //      .headers(headers)
  //      .body(Base64.getUrlEncoder().encodeToString(packetEncoder.encode(normalPacket)))
  //      .willRespondWith()
  //      .status(OK.value())
  //      .headers(singletonMap(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE))
  //      .body(gson.toJson(Response.ok(OK, "Success")))
  //      .toPact();
  //}
  //
  //@Pact(consumer = "temail-gateway", provider = "temail-dispatcher")
  //public RequestResponsePact unregisteredFragment(PactDslWithProvider pactDslWithProvider) {
  //  Map<String, String> headers = new HashMap<>();
  //  headers.put(CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE);
  //
  //  return pactDslWithProvider
  //      .given("User jack is not registered")
  //      .uponReceiving("Jack requested to log in")
  //      .path(path)
  //      .method("POST")
  //      .headers(headers)
  //      .body(Base64.getUrlEncoder().encodeToString(packetEncoder.encode(notRegPacket)))
  //      .willRespondWith()
  //      .status(FORBIDDEN.value())
  //      .headers(singletonMap(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE))
  //      .body(gson.toJson(Response.failed(FORBIDDEN)))
  //      .toPact();
  //}
  //
  //@Pact(consumer = "temail-gateway", provider = "temail-dispatcher")
  //public RequestResponsePact failedFragment(PactDslWithProvider pactDslWithProvider) {
  //  Map<String, String> headers = new HashMap<>();
  //  headers.put(CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE);
  //
  //  return pactDslWithProvider
  //      .given("User mike is registered, but server is out of work")
  //      .uponReceiving("Mike requested to log in")
  //      .path(path)
  //      .method("POST")
  //      .headers(headers)
  //      .body(Base64.getUrlEncoder().encodeToString(packetEncoder.encode(signErrorPacket)))
  //      .willRespondWith()
  //      .status(INTERNAL_SERVER_ERROR.value())
  //      .headers(singletonMap(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE))
  //      .body(gson.toJson(Response.failed(INTERNAL_SERVER_ERROR)))
  //      .toPact();
  //}
  //
  //@Test
  //@PactVerification(value = "temail-dispatcher", fragment = "successFragment")
  //public void runTest() {
  //  String url = mockTestProvider.getUrl() + path;
  //
  //  SuccessConsumer successConsumer = new SuccessConsumer();
  //  FailedConsumer failedConsumer = new FailedConsumer();
  //
  //  AuthService authService = getAuthService(url);
  //  authService.validSignature(packetEncoder.encode(normalPacket), successConsumer, failedConsumer);
  //  waitAtMost(3, SECONDS).until(() -> !resultResponses.isEmpty());
  //  assertThat(resultResponses.poll().getCode()).isEqualTo(OK.value());
  //  assertThat(errorResponses).isEmpty();
  //}
  //
  //@Test
  //@PactVerification(value = "temail-dispatcher", fragment = "unregisteredFragment")
  //public void rejectUnregisteredUser() {
  //  String url = mockTestProvider.getUrl() + path;
  //
  //  SuccessConsumer successConsumer = new SuccessConsumer();
  //  FailedConsumer failedConsumer = new FailedConsumer();
  //
  //  AuthService authService = getAuthService(url);
  //  authService.validSignature(packetEncoder.encode(notRegPacket), successConsumer, failedConsumer);
  //  waitAtMost(3, SECONDS).until(() -> !errorResponses.isEmpty());
  //  assertThat(errorResponses.poll().getCode()).isEqualTo(FORBIDDEN.value());
  //  assertThat(resultResponses).isEmpty();
  //}
  //
  //@Test
  //@PactVerification(value = "temail-dispatcher", fragment = "failedFragment")
  //public void serverOutOfWork() {
  //  String url = mockTestProvider.getUrl() + path;
  //
  //  SuccessConsumer successConsumer = new SuccessConsumer();
  //  FailedConsumer failedConsumer = new FailedConsumer();
  //
  //  AuthService authService = getAuthService(url);
  //
  //  authService.validSignature(packetEncoder.encode(signErrorPacket), successConsumer, failedConsumer);
  //  waitAtMost(3, SECONDS).until(() -> !errorResponses.isEmpty());
  //  assertThat(errorResponses.poll().getCode()).isEqualTo(INTERNAL_SERVER_ERROR.value());
  //  assertThat(resultResponses).isEmpty();
  //}
  //
  //protected abstract AuthService getAuthService(String url);
  //
  //protected class SuccessConsumer implements Consumer<Response> {
  //
  //  @Override
  //  public void accept(Response response) {
  //    resultResponses.offer(response);
  //  }
  //}
  //
  //protected class FailedConsumer implements Consumer<Response> {
  //
  //  @Override
  //  public void accept(Response response) {
  //    errorResponses.offer(response);
  //  }
  //}
}
