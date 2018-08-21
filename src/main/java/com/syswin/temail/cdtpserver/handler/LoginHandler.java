package com.syswin.temail.cdtpserver.handler;

import io.netty.channel.socket.SocketChannel;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
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
import com.google.protobuf.ByteString;
import com.syswin.temail.cdtpserver.connection.ActiveTemailManager;
import com.syswin.temail.cdtpserver.constants.ConstantsAttributeKey;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;
import com.syswin.temail.cdtpserver.entity.CommandEnum;
import com.syswin.temail.cdtpserver.entity.Response;
import com.syswin.temail.cdtpserver.entity.TemailInfo;
import com.syswin.temail.cdtpserver.entity.TemailMqInfo;
import com.syswin.temail.cdtpserver.entity.TemailSocketInfo;
import com.syswin.temail.cdtpserver.entity.TemailSocketOptEnum;
import com.syswin.temail.cdtpserver.handler.base.BaseHandler;
import com.syswin.temail.cdtpserver.properties.TemailServerProperties;
import com.syswin.temail.cdtpserver.status.TemailSocketSyncClient;
import com.syswin.temail.cdtpserver.utils.TemailKeyUtil;
import com.syswin.temail.cdtpserver.utils.TemailSocketBuilderUtil;

/**
 * Created by weis on 18/8/8.
 */
public class LoginHandler extends BaseHandler {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass());

  public LoginHandler(SocketChannel socketChannel, CDTPPackageProto.CDTPPackage cdtpPackage,
      TemailServerProperties temailServerConfig, TemailSocketSyncClient temailSocketSyncClient,
      TemailMqInfo temailMqInfo) {
      super(socketChannel, cdtpPackage, temailServerConfig, temailSocketSyncClient, temailMqInfo);
  }

  @Override
  public void process() {
    login();

  }

  /**
   * 登陆逻辑 1.先判断From合法性 2.调用dispatch服务 3.成功操作状态管理服务 4.失败,返回错误信息,关闭连接
   * 
   * @param cdtpPackage
   * @return
   */
  private void login() {
    LOGGER.info("在登录LoginHandler中收到 cdtp msg {} ", getCdtpPackage().toString());
    Gson gson = new Gson();
    TemailInfo temailInfo =
        gson.fromJson(getCdtpPackage().getData().toStringUtf8(), TemailInfo.class);

    if(StringUtils.isNotBlank(temailInfo.getTemail())  && StringUtils.isNotBlank(temailInfo.getDevId())) {
      Map map = new ConcurrentHashMap<String, String>();
      map.put("temail", temailInfo.getTemail());
      map.put("unsignedBytes", "");
      map.put("signature", "");
      String authDataJson = gson.toJson(map);

      HttpHeaders requestHeaders = new HttpHeaders();
      requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
      HttpEntity<String> requestEntity = new HttpEntity<String>(authDataJson, requestHeaders);
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.setErrorHandler(new SilentResponseErrorHandler());

      ResponseEntity<Response> responseEntity =
          restTemplate.exchange(getTemailServerConfig().getVerifyUrl(), HttpMethod.POST,requestEntity, Response.class);
      Response response = responseEntity.getBody();
      
      if (null != response.getCode() && response.getCode() == HttpStatus.OK.value()) {
        loginSuccess(temailInfo, response);
      } else {
        loginFailure(temailInfo, response);
      }
      
    } else {
      LOGGER.info("在登录指令中, 接收到CDTP package 信息 中  temail:{} 或者 devId:{} 为空, 关闭连接. temailInfo is : {}", temailInfo.getTemail(),  temailInfo.getDevId(), temailInfo);
      
      
      Response<String>  loginErrorResponse = new  Response<String>(HttpStatus.FORBIDDEN, builderLoginErrorInf(temailInfo));
      loginFailure(temailInfo, loginErrorResponse);
    }
  }


  private void loginSuccess(TemailInfo temailInfo, Response response) {
    // 设置session
    String temailKey = TemailKeyUtil.builderTemailKey(temailInfo);
    getSocketChannel().attr(ConstantsAttributeKey.TEMAIL_KEY).set(temailKey);
    temailInfo.setSocketChannel(getSocketChannel());
    temailInfo.setTimestamp(new Timestamp(System.currentTimeMillis()));
    ActiveTemailManager.add(temailKey, temailInfo);

    TemailSocketInfo temailSocketInfo =
        TemailSocketBuilderUtil.temailSocketBuilder(temailInfo, getTemailMqInfo(),
            TemailSocketOptEnum.add.toString());
    getTemailSocketSyncClient().updateTemailSocketInfToRemote(temailSocketInfo);

    CDTPPackage.Builder builder = CDTPPackage.newBuilder();
    builder.setCommand(CommandEnum.connect.getCode());
    builder.setPkgId(getCdtpPackage().getPkgId());
    CDTPPackage newcdtpPackage = builder.build();
    this.getSocketChannel().writeAndFlush(newcdtpPackage);
    LOGGER.info("**********登录成功, the  temial is :{} and  devId is {} , 返回给前端的消息是: {}",
        temailInfo.getTemail(), temailInfo.getDevId(), newcdtpPackage.toString());

  }

  private void loginFailure(TemailInfo temailInfo, Response response) {
    if (null != response) {
      
      CDTPPackage.Builder builder = CDTPPackage.newBuilder();
      builder.setCommand(CommandEnum.connect.getCode());
      builder.setPkgId(getCdtpPackage().getPkgId());
      
      Gson gson = new Gson();
      if(null == response.getCode()){
        response.setCode(HttpStatus.FORBIDDEN.value());
      }
      String  responseJson = gson.toJson(response);            
      builder.setData(ByteString.copyFrom(responseJson, Charset.defaultCharset())); 
      CDTPPackage newcdtpPackage = builder.build();
      
      LOGGER.info("登录失败, 发送 response.getData： {}, response.getMessage:{},  返回给前端的具体报文信息:{} ", response.getData(),
          response.getMessage(), newcdtpPackage.toString());      
      getSocketChannel().writeAndFlush(newcdtpPackage);   
    }
    getSocketChannel().close();
    LOGGER.info("##########登录失败 , the  temial is :{} and  devId is {} , send msg is {} ",
        temailInfo.getTemail(), temailInfo.getDevId(), temailInfo.toString());
  }


  private  String  builderLoginErrorInf(TemailInfo temailInfo){
      StringBuffer  errorInfBuffer  =  new StringBuffer();
      if(null != temailInfo){
         if(StringUtils.isBlank(temailInfo.getTemail())){
           errorInfBuffer.append("temail 信息为空   ");
         }
         
         if(StringUtils.isBlank(temailInfo.getDevId())){
           errorInfBuffer.append("devId 信息为空   ");
         }
      }
      else{
        errorInfBuffer.append("Temail报文信息为空");
      }
      return  errorInfBuffer.toString();
  }
}
