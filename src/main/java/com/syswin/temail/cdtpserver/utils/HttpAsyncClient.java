package com.syswin.temail.cdtpserver.utils;

import java.lang.invoke.MethodHandles;
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
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;
import com.syswin.temail.cdtpserver.entity.TransferCDTPPackage;
import com.syswin.temail.cdtpserver.exception.CdtpServerException;
public class HttpAsyncClient {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass());

  public static  void post(String url, TransferCDTPPackage transferCDTPPackage){
    callHttpService(url, transferCDTPPackage);
  }


  private static void callHttpService(String url, final TransferCDTPPackage transferCDTPPackage){
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
          LOGGER.info("route info  success.");
        }

        @Override
        public void failed(final Exception ex) {
          latch.countDown();
          LOGGER.info("route info  failed.");
        }

        @Override
        public void cancelled() {
          latch.countDown();
          LOGGER.info("route info  cancelled.");
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

}
