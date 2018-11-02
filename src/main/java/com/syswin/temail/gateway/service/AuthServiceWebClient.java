package com.syswin.temail.gateway.service;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

import com.google.gson.Gson;
import com.syswin.temail.gateway.codec.CDTPPacketConverter;
import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
public class AuthServiceWebClient implements AuthService {

  private final WebClient webClient;
  private final String authUrl;
  private final Gson gson = new Gson();

  public AuthServiceWebClient(WebClient webClient, String authUrl) {
    this.webClient = webClient;
    this.authUrl = authUrl;
  }

  public AuthServiceWebClient(String url) {
    this(WebClient.create(), url);
  }

  @Override
  public void validSignature(CDTPPacket reqPacket, Consumer<Response> sucessConsumer,
      Consumer<Response> failedConsumer) {
    CDTPPacketTrans packetTrans = CDTPPacketConverter.toTrans(reqPacket);
    webClient.post()
        .uri(authUrl)
        .contentType(APPLICATION_JSON_UTF8)
        .syncBody(gson.toJson(packetTrans))
        .exchange()
        .subscribe(
            clientResponse -> {
              if (clientResponse.statusCode().is2xxSuccessful()) {
                clientResponse.bodyToMono(Response.class)
                    .subscribe(
                        sucessConsumer,
                        throwable -> failedConsumer.accept(getFailedResponse(throwable))
                    );
              } else {
                clientResponse.bodyToMono(Response.class)
                    .subscribe(
                        failedConsumer,
                        throwable -> failedConsumer.accept(getFailedResponse(throwable))
                    );
              }
            },
            throwable -> failedConsumer.accept(getFailedResponse(throwable))
        );
  }

  private Response<Object> getFailedResponse(Throwable throwable) {
    return Response.failed(HttpStatus.BAD_REQUEST, throwable.getMessage());
  }

}
