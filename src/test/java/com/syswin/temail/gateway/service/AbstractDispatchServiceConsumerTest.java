package com.syswin.temail.gateway.service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractDispatchServiceConsumerTest /*extends ConsumerPactTestMk2*/ {

  //private static final String ackMessage = "Sent ackMessage";
  //private final String path = "/dispatch";
  //private final Gson gson = new Gson();
  //private final String sender = "jack@t.email";
  //private final String receiver = "sean@t.email";
  //private final String message = "hello world";
  //private final String deviceId = "deviceId_5514";
  //private final CDTPPacket packet = singleChatPacket(sender, receiver, message, deviceId);
  //private final PacketEncoder packetEncoder = new PacketEncoder();
  //private volatile Response resultResponse = null;
  //private Throwable exception;
  //
  //@Override
  //public RequestResponsePact createPact(PactDslWithProvider pactDslWithProvider) {
  //  Map<String, String> headers = new HashMap<>();
  //  headers.put(CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE);
  //
  //  return pactDslWithProvider
  //      .given("dispatch user request")
  //      .uponReceiving("dispatch user request for response")
  //      .method("POST")
  //      .body(Base64.getUrlEncoder().encodeToString(packetEncoder.encode(packet)))
  //      .headers(headers)
  //      .path(path)
  //      .willRespondWith()
  //      .status(200)
  //      .headers(singletonMap(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE))
  //      .body(gson.toJson(Response.ok(OK, ackPayload())))
  //      .toPact();
  //}
  //
  //@Override
  //public void runTest(MockServer mockServer) {
  //  String url = mockServer.getUrl() + path;
  //  DispatchService dispatchService = getDispatchService(url);
  //  dispatchService.dispatch(packetEncoder.encode(packet), new ResponseConsumer(), new ErrorConsumer());
  //
  //  waitAtMost(2, SECONDS).until(() -> resultResponse != null);
  //  log.info("result code is {},  msg  is {}", resultResponse.getCode(), resultResponse.getMessage());
  //
  //  assertThat(resultResponse.getCode()).isEqualTo(OK.value());
  //
  //  String errorUrl = "http://localhost:99";
  //  DispatchService errorDispatchService = getDispatchService(errorUrl);
  //  errorDispatchService.dispatch(packetEncoder.encode(packet), new ResponseConsumer(), new ErrorConsumer());
  //
  //  waitAtMost(2, SECONDS).until(() -> exception != null);
  //}
  //
  //protected abstract DispatchService getDispatchService(String url);
  //
  //@Override
  //protected String providerName() {
  //  return "temail-dispatcher";
  //}
  //
  //@Override
  //protected String consumerName() {
  //  return "temail-gateway";
  //}
  //
  //@NotNull
  //private CDTPPacket ackPayload() {
  //  CDTPPacket payload = new CDTPPacket();
  //  payload.setCommandSpace(SINGLE_MESSAGE_CODE);
  //  payload.setCommand(SEND_MESSAGE.getCode());
  //  payload.setData(gson.toJson(Response.ok(ackMessage)).getBytes());
  //  return payload;
  //}
  //
  //private class ResponseConsumer implements Consumer<byte[]> {
  //
  //  @Override
  //  public void accept(byte[] bytes) {
  //    resultResponse = gson.fromJson(new String(bytes), Response.class);
  //  }
  //}
  //
  //private class ErrorConsumer implements Consumer<Throwable> {
  //
  //  @Override
  //  public void accept(Throwable t) {
  //    exception = t;
  //  }
  //}
}
