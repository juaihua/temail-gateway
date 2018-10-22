package com.syswin.temail.gateway.service;

import com.google.gson.Gson;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
class DispatchServiceWebClient implements DispatchService<ClientResponse> {

  private final WebClient webClient;
  private final String dispatchUrl;
  private Gson gson = new Gson();

  DispatchServiceWebClient(WebClient webClient, String dispatchUrl) {
    this.webClient = webClient;
    this.dispatchUrl = dispatchUrl;
  }

  DispatchServiceWebClient(String dispatchUrl) {
    this.webClient = WebClient.create();
    this.dispatchUrl = dispatchUrl;
  }

  @Override
  public void dispatch(CDTPPacketTrans packet,
      Consumer<? super ClientResponse> consumer,
      Consumer<? super Throwable> errorConsumer) {
    webClient.post()
        .uri(dispatchUrl)
        .contentType(MediaType.APPLICATION_JSON)
        .syncBody(gson.toJson(
            packet)) // 此处使用gson进行对象转换，是因为WebClient默认使用jackson进行json转换，与服务端的json转换不一致。jackson与gson处理字节数组行为不一致。
        .exchange()
        .subscribe(consumer, errorConsumer);
  }
}
