package com.syswin.temail.cdtpserver.handler;

import io.netty.channel.socket.SocketChannel;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;

import com.google.gson.Gson;
import com.syswin.temail.cdtpserver.connection.ActiveTemailManager;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;
import com.syswin.temail.cdtpserver.entity.TemailInfo;
import com.syswin.temail.cdtpserver.handler.base.BaseHandler;
import com.syswin.temail.cdtpserver.properties.TemailServerProperties;

public class DisconnectHandler extends BaseHandler {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  

  public DisconnectHandler(SocketChannel socketChannel, CDTPPackage cdtpPackage, TemailServerProperties temailServerConfig) {
      super(socketChannel, cdtpPackage, temailServerConfig);
  }

  @Override
  public void process() {  
   
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setContentType(MediaType.APPLICATION_JSON);  
    
    HttpEntity<CDTPPackage> requestEntity = new HttpEntity<CDTPPackage>(getCdtpPackage() , requestHeaders);
    AsyncRestTemplate asyncRt = new AsyncRestTemplate();
    
    ListenableFuture<ResponseEntity<String>> future = asyncRt.postForEntity(getTemailServerConfig().getDispatchUrl(), requestEntity, String.class);
    future.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {
        public void onSuccess(ResponseEntity<String> resp) {
           String  result = resp.getBody();
           LOGGER.info("logout  call back msg is{} ", result);
        }
        public void onFailure(Throwable t) {
          LOGGER.error("logout  call back msg is{} ", t.getMessage());
        }
    });
    
    Gson gson = new Gson();
    TemailInfo temailInfo =
        gson.fromJson(getCdtpPackage().getData().toStringUtf8(), TemailInfo.class); 
       String temailKey = temailInfo.getTemail() + "-" + temailInfo.getDevId();
    ActiveTemailManager.remove(temailKey);   
    getSocketChannel().close();
    
    LOGGER.info("disconnection, close  SocketChannel {}",this.getSocketChannel().attr(ConstantsAttributeKey.TEMAIL_KEY).get());   
  }

}
