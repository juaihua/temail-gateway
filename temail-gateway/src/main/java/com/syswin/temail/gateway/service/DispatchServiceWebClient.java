package com.syswin.temail.gateway.service;

import com.google.gson.Gson;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
public class DispatchServiceWebClient implements DispatchService {

  private final WebClient webClient;
  private final String dispatchUrl;
  private Gson gson = new Gson();

  public DispatchServiceWebClient(WebClient webClient, String dispatchUrl) {
    this.webClient = webClient;
    this.dispatchUrl = dispatchUrl;
  }

  public DispatchServiceWebClient(String dispatchUrl) {
    this.webClient = WebClient.create();
    this.dispatchUrl = dispatchUrl;
  }

  @Override
  public void dispatch(CDTPPacketTrans packet,
      Consumer<byte[]> consumer,
      Consumer<? super Throwable> errorConsumer) {
    webClient.post()
        .uri(dispatchUrl)
        .contentType(MediaType.APPLICATION_JSON)
        // 此处使用gson进行对象转换，是因为WebClient默认使用jackson进行json转换，与服务端的json转换不一致。jackson与gson处理字节数组行为不一致。
        .syncBody(gson.toJson(packet))
        .exchange()
        .subscribe(
            clientResponse ->
                clientResponse.bodyToMono(String.class)
                    .subscribe(response -> {
                      if (response != null) {
                        // 后台正常返回
                        consumer.accept(response.getBytes());
                      } else {
                        errorConsumer.accept(new RuntimeException("dispatcher请求没有从服务器端返回结果对象"));
                      }
                    }),
            errorConsumer
        );
  }
}
