package com.syswin.temail.gateway.service;

import com.google.gson.Gson;
import com.syswin.temail.gateway.entity.CDTPPacketTrans;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
class DispatchService {

  private final WebClient webClient;
  private final Gson gson;
  DispatchService(WebClient webClient) {
    this.webClient = webClient;
    this.gson = new Gson();
  }

  void dispatch(String dispatchUrl, CDTPPacketTrans packet,
      Consumer<? super ClientResponse> consumer,
      Consumer<? super Throwable> errorConsumer) {
    webClient.post()
        .uri(dispatchUrl)
        .contentType(MediaType.APPLICATION_JSON)
        .syncBody(gson.toJson(packet))
        .exchange()
        .subscribe(consumer, errorConsumer);
  }
}
