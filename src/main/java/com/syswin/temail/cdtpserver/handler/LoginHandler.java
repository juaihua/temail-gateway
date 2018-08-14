package com.syswin.temail.cdtpserver.handler;

import io.netty.channel.socket.SocketChannel;

import java.lang.invoke.MethodHandles;
import java.sql.Timestamp;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.syswin.temail.cdtpserver.entity.ActiveTemailManager;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;
import com.syswin.temail.cdtpserver.entity.Response;
import com.syswin.temail.cdtpserver.entity.TemailInfo;
import com.syswin.temail.cdtpserver.utils.ConstantsAttributeKey;

/**
 * Created by weis on 18/8/8.
 */
public class LoginHandler extends BaseHandler {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass());

  @Resource
  private RestTemplate restTemplate;
 
  //@Value("${login.verifyUrl}")  
  private String  verifyUrl = "http://172.31.245.225:8888/verify";
  
  public LoginHandler(SocketChannel socketChannel, CDTPPackageProto.CDTPPackage cdtpPackage) {
    super(socketChannel, cdtpPackage);
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
    LOGGER.info("in  LoginHandler receive cdtp msg:" + getCdtpPackage().toString());
    Gson gson = new Gson();
    TemailInfo temailInfo =
        gson.fromJson(getCdtpPackage().getData().toStringUtf8(), TemailInfo.class);
    
    String  cdtpPackageJson = gson.toJson(getCdtpPackage());    
    HttpHeaders requestHeaders = new HttpHeaders();
    //requestHeaders.setContentType(MediaType.APPLICATION_JSON); 
    requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8); 
    HttpEntity<String> requestEntity = new HttpEntity<String>(cdtpPackageJson, requestHeaders);
    
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setErrorHandler(new SilentResponseErrorHandler());
    
    ResponseEntity<Response> responseEntity =
        restTemplate.exchange(verifyUrl, HttpMethod.POST, requestEntity, Response.class);
    
    //ResponseEntity<Context> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity,Context.class);

    Response  response = responseEntity.getBody();
    if(response.getCode().equals(HttpStatus.OK)){
       loginSuccess(temailInfo, response);
    }
    else{
       loginFailure(temailInfo, response);
    } 
    
  }
  
  
  private void  loginSuccess(TemailInfo temailInfo, Response  response){
    // 设置session
    String temailKey = temailInfo.getTemail() + "-" + temailInfo.getDevId();
    getSocketChannel().attr(ConstantsAttributeKey.TEMAIL_KEY).set(temailKey);
    temailInfo.setSocketChannel(getSocketChannel());
    temailInfo.setTimestamp(new Timestamp(System.currentTimeMillis()));
    ActiveTemailManager.add(temailKey, temailInfo);

    /*CDTPPackage.Builder builder = CDTPPackage.newBuilder();
    builder.setCommand(CommandEnum.resp.getCode());
    builder.setPkgId(getCdtpPackage().getPkgId());
    CDTPPackage newcdtpPackage = builder.build();
    LOGGER.info("send login success msg:" + newcdtpPackage.toString());*/
    getSocketChannel().writeAndFlush(response.getData());    
    LOGGER.info("login  success , the  temial is :{} and  devId is {} , send  msg is {}", temailInfo.getTemail(),  temailInfo.getDevId(), response.toString());
  }
  
  private  void  loginFailure(TemailInfo temailInfo, Response  response){
    getSocketChannel().writeAndFlush(response.getData());
    getSocketChannel().close();
    LOGGER.info("login  fail , the  temial is :{} and  devId is {} , send msg is {} ", temailInfo.getTemail(),  temailInfo.getDevId(), temailInfo.toString());
  }


}
