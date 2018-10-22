package com.syswin.temail.gateway.service;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import com.google.gson.Gson;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;

/**
 * @author 姚华成
 * @date 2018-10-22
 */
class DispatchServiceHttpClientAsync implements DispatchService<HttpResponse> {

  private final CloseableHttpAsyncClient asyncClient;
  private final String dispatchUrl;
  private final Gson gson = new Gson();

  public DispatchServiceHttpClientAsync(CloseableHttpAsyncClient asyncClient, String dispatchUrl) {
    this.asyncClient = asyncClient;
    this.dispatchUrl = dispatchUrl;
  }

  public DispatchServiceHttpClientAsync(String dispatchUrl) {
    this.asyncClient = HttpAsyncClientBuilder.create().build();
    this.asyncClient.start();
    this.dispatchUrl = dispatchUrl;
  }

  @Override
  public void dispatch(CDTPPacketTrans packet, Consumer<? super HttpResponse> consumer,
      Consumer<? super Throwable> errorConsumer) {
    StringEntity bodyEntity = new StringEntity(gson.toJson(packet), StandardCharsets.UTF_8);
    bodyEntity.setContentType(APPLICATION_JSON_VALUE);
    HttpPost request = new HttpPost(dispatchUrl);
    request.setEntity(bodyEntity);

    asyncClient.execute(request, new FutureCallback<HttpResponse>() {
      @Override
      public void completed(HttpResponse result) {
        consumer.accept(result);
      }

      @Override
      public void failed(Exception ex) {
        errorConsumer.accept(ex);
      }

      @Override
      public void cancelled() {
        throw new UnsupportedOperationException("暂时不支持取消操作");
      }
    });
  }
}
