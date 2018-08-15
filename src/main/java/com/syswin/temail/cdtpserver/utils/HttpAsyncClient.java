package com.syswin.temail.cdtpserver.utils;

import io.netty.channel.socket.SocketChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.ByteString;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;
import com.syswin.temail.cdtpserver.entity.Response;
import com.syswin.temail.cdtpserver.entity.TransferCDTPPackage;
import com.syswin.temail.cdtpserver.exception.CdtpServerException;

public class HttpAsyncClient {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass());

  public static void post(String url, TransferCDTPPackage transferCDTPPackage,
      SocketChannel socketChannel) {
    callHttpService(url, transferCDTPPackage, socketChannel);
  }


  private static void callHttpService(String url, final TransferCDTPPackage transferCDTPPackage,
      SocketChannel socketChannel) {
    CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
    try {
      httpclient.start();

      final HttpPost request = new HttpPost(url);
      Gson gson = new Gson();
      String cdtpPackageJson = gson.toJson(transferCDTPPackage);


      StringEntity entitySender = new StringEntity(cdtpPackageJson, "UTF-8");
      request.setEntity(entitySender);
      request.setHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
      final CountDownLatch latch = new CountDownLatch(1);
      httpclient.execute(request, new FutureCallback<HttpResponse>() {

        @Override
        public void completed(final HttpResponse response) {
          latch.countDown();
          HttpEntity entity = response.getEntity();
          String resultStr = "";
          if (entity != null) {
            InputStream instreams;
            try {
              instreams = entity.getContent();
              resultStr = convertStreamToString(instreams);
              TransferCDTPPackage transferCDTPPackage =
                  convertStringToTransferCDTPPackage(resultStr);
              CDTPPackage.Builder builder = CDTPPackage.newBuilder();
              copyBeanProperties(transferCDTPPackage, builder);
              CDTPPackage ctPackage = builder.build();
              socketChannel.writeAndFlush(ctPackage);
            } catch (UnsupportedOperationException ex) {
              LOGGER.error("execute ReqHandler call back  exception", ex);
            } catch (IOException ex) {
              LOGGER.error("execute ReqHandler call back  exception", ex);
            }
          }
          LOGGER.info("route info  success.");
        }

        @Override
        public void failed(final Exception ex) {
          latch.countDown();
          LOGGER.error("execute ReqHandler call back  fail", ex);
        }

        @Override
        public void cancelled() {
          latch.countDown();
          LOGGER.error("execute ReqHandler call back  fcancelled");
        }

      });
      latch.await();
      httpclient.close();
    } catch (CdtpServerException ex) {
      throw new CdtpServerException("route url:{} error" + url, ex);
    } catch (Exception ex) {
      throw new CdtpServerException("route url:{} error" + url, ex);
    }
  }

  public static String convertStreamToString(InputStream is) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuilder sb = new StringBuilder();
    String line = null;
    try {
      while ((line = reader.readLine()) != null) {
        sb.append(line + "");
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return sb.toString();
  }



  public static TransferCDTPPackage convertStringToTransferCDTPPackage(String msgData) {
    Gson gson = new Gson();

    Response<TransferCDTPPackage> response =
        gson.fromJson(msgData, new TypeToken<Response<TransferCDTPPackage>>() {}.getType());// 把JSON字符串转为对象
    return response.getData();

  }

  public static void copyBeanProperties(TransferCDTPPackage transferCDTPPackage,
      CDTPPackage.Builder builder) {

    builder.setCommand(transferCDTPPackage.getCommand());
    builder.setVersion(transferCDTPPackage.getVersion());
    builder.setAlgorithm(transferCDTPPackage.getAlgorithm());
    if (null != transferCDTPPackage.getSign()) {
      builder.setSign(transferCDTPPackage.getSign());
    }

    builder.setDem(transferCDTPPackage.getDem());
    builder.setTimestamp(transferCDTPPackage.getTimestamp());
    if (null != transferCDTPPackage.getPkgId()) {
      builder.setPkgId(transferCDTPPackage.getPkgId());
    }

    if (null != transferCDTPPackage.getFrom()) {
      builder.setFrom(transferCDTPPackage.getFrom());
    }
    if (null != transferCDTPPackage.getTo()) {
      builder.setTo(transferCDTPPackage.getTo());
    }

    if (null != transferCDTPPackage.getSenderPK()) {
      builder.setSenderPK(transferCDTPPackage.getSenderPK());
    }

    if (null != transferCDTPPackage.getReceiverPK()) {
      builder.setReceiverPK(transferCDTPPackage.getReceiverPK());
    }


    builder.setData(ByteString.copyFrom(transferCDTPPackage.getData(), Charset.defaultCharset()));
  }



}
