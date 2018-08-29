package com.syswin.temail.gateway.service;

import com.google.gson.Gson;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import javax.annotation.Resource;
import static com.syswin.temail.gateway.entity.CommandType.INTERNAL_ERROR;

@Slf4j
@Service
public class DispatchService {

  private  final WebClient dispatcherWebClient;
  private final Gson gson = new Gson();

  public  DispatchService(WebClient dispatcherWebClient){

    this.dispatcherWebClient = dispatcherWebClient;
  }

  public void dispatch(CDTPPacket packet, String dispatchUrl, DispatchCallback dispatchCallback) {


    dispatcherWebClient.post()
        .uri(dispatchUrl)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .syncBody(gson.toJson(packet))
        .exchange()
        .subscribe(clientResponse -> {
          clientResponse
              .bodyToMono(new ParameterizedTypeReference<Response<CDTPPacket>>() {
              })
              .subscribe(resp -> {
                dispatchCallback.onsuccess(resp);
              });
        }, t -> {
          dispatchCallback.onError(INTERNAL_ERROR.getCode(), t.getMessage());
        });


  }


}
