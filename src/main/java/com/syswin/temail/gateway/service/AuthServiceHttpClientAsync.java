package com.syswin.temail.gateway.service;

import static org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM;

import com.google.gson.Gson;
import com.syswin.temail.gateway.entity.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;

public class AuthServiceHttpClientAsync implements AuthService {

  private final HttpAsyncClient asyncClient;
  private final String authUrl;
  private final Function<byte[], HttpEntity> httpEntitySupplier;
  private final Gson gson = new Gson();

  public AuthServiceHttpClientAsync(String authUrl, HttpAsyncClient asyncClient) {
    this(authUrl, asyncClient, bytes -> new ByteArrayEntity(bytes, APPLICATION_OCTET_STREAM));
  }

  AuthServiceHttpClientAsync(String authUrl,
      HttpAsyncClient asyncClient,
      Function<byte[], HttpEntity> httpEntitySupplier) {
    this.asyncClient = asyncClient;
    this.authUrl = authUrl;
    this.httpEntitySupplier = httpEntitySupplier;
  }

  @Override
  public void validSignature(byte[] payload, Consumer<Response> successConsumer,
      Consumer<Response> failedConsumer) {

    HttpEntity bodyEntity = httpEntitySupplier.apply(payload);
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
        throw new UnsupportedOperationException("???????????????????????????");
      }
    });
  }
}
