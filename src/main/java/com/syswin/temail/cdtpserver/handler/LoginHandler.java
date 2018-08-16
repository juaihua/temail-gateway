package com.syswin.temail.cdtpserver.handler;

import io.netty.channel.socket.SocketChannel;

import java.lang.invoke.MethodHandles;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.syswin.temail.cdtpserver.config.TemailServerConfig;
import com.syswin.temail.cdtpserver.entity.ActiveTemailManager;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;
import com.syswin.temail.cdtpserver.entity.Response;
import com.syswin.temail.cdtpserver.entity.TemailInfo;
import com.syswin.temail.cdtpserver.utils.CommandEnum;
import com.syswin.temail.cdtpserver.utils.ConstantsAttributeKey;

/**
 * Created by weis on 18/8/8.
 */
public class LoginHandler extends BaseHandler {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass());
 
  public LoginHandler(SocketChannel socketChannel, CDTPPackageProto.CDTPPackage cdtpPackage, TemailServerConfig   temailServerConfig) {
    super(socketChannel, cdtpPackage, temailServerConfig);
  }

  @Override
  public void process() { 
        login();
 
  }

  /**
   * 登陆逻辑
   * 
   * @param cdtpPackage
   * @return
   */
  private void  login() {    
    /**
     * 1.先判断From合法性 
     * 2.调用dispatch服务 
     * 3.成功操作状态管理服务 
     * 4.失败,返回错误信息,关闭连接
     */    
    LOGGER.info("在登录LoginHandler中收到 cdtp msg {} ", getCdtpPackage().toString());
    Gson gson = new Gson();
    TemailInfo temailInfo =
        gson.fromJson(getCdtpPackage().getData().toStringUtf8(), TemailInfo.class);
    
    if(null != temailInfo.getTemail()){
      Map map =  new ConcurrentHashMap<String, String>();  
      map.put("temail", temailInfo.getTemail());  
      map.put("unsignedBytes", "");
      map.put("signature", "");
      String  authDataJson = gson.toJson(map); 
      
      HttpHeaders requestHeaders = new HttpHeaders();
      requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8); 
      
      HttpEntity<String> requestEntity = new HttpEntity<String>(authDataJson, requestHeaders);
      
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.setErrorHandler(new SilentResponseErrorHandler());

      LOGGER.info("verifyUrl is :", getTemailServerConfig().getVerifyUrl());
      ResponseEntity<Response> responseEntity =
          restTemplate.exchange(getTemailServerConfig().getVerifyUrl(), HttpMethod.POST, requestEntity, Response.class);
      Response  response = responseEntity.getBody();
      if(null != response.getCode() && response.getCode()==HttpStatus.OK.value()){
         loginSuccess(temailInfo, response);
      }
      else{
         loginFailure(temailInfo, response);
      } 
    }
    else{
        LOGGER.info("socketChannel接收到CDTP package 中  temail 信息为空, 关闭连接.",  getSocketChannel().remoteAddress().toString());
        loginFailure(temailInfo, null);
    }   
  }
  
  
  private void  loginSuccess(TemailInfo temailInfo, Response  response){
    // 设置session
    String temailKey = temailInfo.getTemail() + "-" + temailInfo.getDevId();
    getSocketChannel().attr(ConstantsAttributeKey.TEMAIL_KEY).set(temailKey);
    temailInfo.setSocketChannel(getSocketChannel());
    temailInfo.setTimestamp(new Timestamp(System.currentTimeMillis()));
    ActiveTemailManager.add(temailKey, temailInfo);

    CDTPPackage.Builder builder = CDTPPackage.newBuilder();
    builder.setCommand(CommandEnum.connect.getCode());
    builder.setPkgId(getCdtpPackage().getPkgId());
    CDTPPackage newcdtpPackage = builder.build();
    this.getSocketChannel().writeAndFlush(newcdtpPackage);    
    LOGGER.info("**********登录成功, the  temial is :{} and  devId is {} , send  msg is {}", temailInfo.getTemail(),  temailInfo.getDevId(), newcdtpPackage.toString());
  }
  
  private  void  loginFailure(TemailInfo temailInfo, Response  response){
      if(null != response && null != response.getData()){
        LOGGER.info("登录失败, 发送 response.getData： {}", response.getData()); 
        getSocketChannel().writeAndFlush(response.getData());     
      }    
    getSocketChannel().close();
    LOGGER.info("##########登录失败 , the  temial is :{} and  devId is {} , send msg is {} ", temailInfo.getTemail(),  temailInfo.getDevId(), temailInfo.toString());
  }


}
