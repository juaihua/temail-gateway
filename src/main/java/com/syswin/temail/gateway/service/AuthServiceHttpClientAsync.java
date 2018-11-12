package com.syswin.temail.gateway.service;

import static org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM;

import com.google.gson.Gson;
import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;

public class AuthServiceHttpClientAsync implements AuthService {

  private final CloseableHttpAsyncClient asyncClient;
  private final String authUrl;
  private final Gson gson = new Gson();

  public AuthServiceHttpClientAsync(String authUrl) {
    this.asyncClient = HttpAsyncClientBuilder.create().build();
    this.asyncClient.start();
    this.authUrl = authUrl;
  }

  @Override
  public void validSignature(CDTPPacket reqPacket, Consumer<Response> successConsumer,
      Consumer<Response> failedConsumer) {

    ByteArrayEntity bodyEntity = new ByteArrayEntity(reqPacket.getData(), APPLICATION_OCTET_STREAM);
    HttpPost request = new HttpPost(authUrl);
    request.setEntity(bodyEntity);

    asyncClient.execute(request, new FutureCallback<HttpResponse>() {
      @Override
      public void completed(HttpResponse result) {
        try {
          String responseJson = new String(EntityUtils.toByteArray(result.getEntity()), StandardCharsets.UTF_8);
          Response response = gson.fromJson(responseJson, Response.class);
          int statusCode = result.getStatusLine().getStatusCode();
          if (statusCode >= 200 && statusCode <= 299) {
            successConsumer.accept(response);
          } else {
            failedConsumer.accept(response);
          }
        } catch (IOException e) {
          failedConsumer.accept(Response.failed(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
      }

      @Override
      public void failed(Exception e) {
        failedConsumer.accept(Response.failed(HttpStatus.BAD_REQUEST, e.getMessage()));
      }

      @Override
      public void cancelled() {
        throw new UnsupportedOperationException("暂时不支持取消操作");
      }
    });
  }
}
