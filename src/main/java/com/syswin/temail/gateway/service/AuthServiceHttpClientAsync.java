package com.syswin.temail.gateway.service;

import com.google.gson.Gson;
import com.syswin.temail.gateway.codec.CDTPPacketConverter;
import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.ps.common.entity.CDTPPacket;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * @author 姚华成
 * @date 2018-10-22
 */
public class AuthServiceHttpClientAsync implements AuthService {

  private final CloseableHttpAsyncClient asyncClient;
  private final String authUrl;
  private final Gson gson = new Gson();

  public AuthServiceHttpClientAsync(CloseableHttpAsyncClient asyncClient, String authUrl) {
    this.asyncClient = asyncClient;
    this.authUrl = authUrl;
  }

  public AuthServiceHttpClientAsync(String authUrl) {
    this.asyncClient = HttpAsyncClientBuilder.create().build();
    this.asyncClient.start();
    this.authUrl = authUrl;
  }

  @Override
  public void validSignature(CDTPPacket reqPacket, Consumer<Response> sucessConsumer,
      Consumer<Response> failedConsumer) {
    CDTPPacketTrans packetTrans = CDTPPacketConverter.toTrans(reqPacket);

    StringEntity bodyEntity = new StringEntity(gson.toJson(packetTrans), StandardCharsets.UTF_8);
    bodyEntity.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
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
            sucessConsumer.accept(response);
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
