package com.syswin.temail.gateway.service;

import static org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.io.IOException;
import java.util.function.Consumer;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.util.EntityUtils;

public class DispatchServiceHttpClientAsync implements DispatchService {

  private final CloseableHttpAsyncClient asyncClient;
  private final String dispatchUrl;

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
  public void dispatch(CDTPPacket packet, Consumer<byte[]> consumer,
      Consumer<? super Throwable> errorConsumer) {
    ByteArrayEntity bodyEntity = new ByteArrayEntity(packet.getData(), APPLICATION_OCTET_STREAM);
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
