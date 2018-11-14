package com.syswin.temail.gateway.service;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import com.google.gson.Gson;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 * @author 姚华成
 * @date 2018-10-22
 */
public class DispatchServiceHttpClientAsync implements DispatchService {

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
  public void dispatch(CDTPPacketTrans packet, Consumer<byte[]> consumer,
      Consumer<? super Throwable> errorConsumer) {
    StringEntity bodyEntity = new StringEntity(gson.toJson(packet), StandardCharsets.UTF_8);
    bodyEntity.setContentType(APPLICATION_JSON_UTF8_VALUE);
    HttpPost request = new HttpPost(dispatchUrl);
    request.setEntity(bodyEntity);

    asyncClient.execute(request, new FutureCallback<HttpResponse>() {
      @Override
      public void completed(HttpResponse result) {
        try {
          consumer.accept(EntityUtils.toByteArray(result.getEntity()));
        } catch (IOException e) {
          errorConsumer.accept(e);
        }
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
