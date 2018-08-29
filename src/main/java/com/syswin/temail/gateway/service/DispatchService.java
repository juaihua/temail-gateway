package com.syswin.temail.gateway.service;

import com.google.gson.Gson;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
class DispatchService {

  private final WebClient webClient;
  private final Gson gson = new Gson();
  private final ParameterizedTypeReference<Response<CDTPPacket>> typeReference = new ParameterizedTypeReference<Response<CDTPPacket>>() {
  };

  DispatchService(WebClient webClient) {
    this.webClient = webClient;
  }

  void dispatch(String dispatchUrl, CDTPPacket packet, DispatchCallback dispatchCallback) {

    webClient.post()
        .uri(dispatchUrl)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .syncBody(gson.toJson(packet))
        .exchange()
        .subscribe(
            clientResponse -> clientResponse.bodyToMono(typeReference)
                .subscribe(dispatchCallback::onSuccess),
            dispatchCallback::onError);
  }
}
