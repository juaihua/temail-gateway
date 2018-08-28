package com.syswin.temail.gateway.service;


import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPProtoBuf.CDTPLogin;
import com.syswin.temail.gateway.entity.CDTPProtoBuf.CDTPLoginResp;
import com.syswin.temail.gateway.entity.CDTPProtoBuf.CDTPLogoutResp;
import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.gateway.entity.Session;
import com.syswin.temail.gateway.exception.PacketException;
import io.netty.channel.Channel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author 姚华成
 * @date 2018-8-24
 */
@Service
public class SessionService {

  private Gson gson = new Gson();

  @Resource
  private ChannelHolder channelHolder;
  @Resource
  private RemoteStatusService remoteStatusService;
  @Resource
  private RestTemplate restTemplate;
  @Resource
  private TemailGatewayProperties properties;

  /**
   * 正常用户登录: <br> 登陆逻辑 1.先判断From合法性 2.调用dispatch服务 3.成功操作状态管理服务 4.失败,返回错误信息,关闭连接
   */
  public void login(Channel channel, CDTPPacket packet) {
    // TODO(姚华成) 对packet进行合法性校验
    Map<String, String> map = new HashMap<>();
    // TODO 当前认证请求做简化处理，未来需要完善
    String temail = packet.getHeader().getSender();
    map.put("temail", temail);
    map.put("unsignedBytes", "");
    map.put("signature", "");
    String authDataJson = gson.toJson(map);

    try {
      CDTPLogin cdtpLogin = CDTPLogin.parseFrom(packet.getData());
    } catch (InvalidProtocolBufferException e) {
      throw new PacketException(e);
    }
    // TODO(姚华成): 这个cdtpLogin对象干什么用的？

//    HttpHeaders requestHeaders = new HttpHeaders();
//    requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);

//    HttpEntity<String> requestEntity = new HttpEntity<>(authDataJson, requestHeaders);
//
//    ResponseEntity<Response> responseEntity = restTemplate
//        .postForEntity(properties.getVerifyUrl(), requestEntity, Response.class);
//    Response response = responseEntity.getBody();
    Response response = Response.ok();
    ResponseEntity<Response> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
    if (responseEntity.getStatusCode().is2xxSuccessful()) {
      loginSuccess(channel, packet, response);
    } else {
      loginFailure(channel, packet, response);
    }
  }

  private void loginSuccess(Channel channel, CDTPPacket packet, Response response) {
    String temail = packet.getHeader().getSender();
    String deviceId = packet.getHeader().getDeviceId();
    channelHolder.addSession(temail, deviceId, channel);
    remoteStatusService.addSession(temail, deviceId);
    // 返回成功的消息
    CDTPLoginResp.Builder builder = CDTPLoginResp.newBuilder();
    builder.setCode(response == null ? HttpStatus.OK.value() : response.getCode());
    if(response!=null&&response.getMessage()!=null)
    builder.setDesc(response.getMessage());
    packet.setData(builder.build().toByteArray());
    channel.writeAndFlush(packet);
  }

  private void loginFailure(Channel channel, CDTPPacket packet, Response response) {
    // 登录失败返回错误消息，然后检查当前通道是否有登录用户，没有则关闭
    CDTPLoginResp.Builder builder = CDTPLoginResp.newBuilder();
    builder.setCode(response == null ? HttpStatus.OK.value() : response.getCode());
    builder.setDesc(response == null ? null : response.getMessage());
    packet.setData(builder.build().toByteArray());
    channel.writeAndFlush(packet);

    if (channelHolder.hasNoSession(channel)) {
      channel.close();
    }
  }

  /**
   * 用户主动登出
   *
   * @param channel 用户连接通道
   * @param packet 用户请求数据包
   */
  public void logout(Channel channel, CDTPPacket packet) {
    // TODO(姚华成) 对packet进行合法性校验
    String temail = packet.getHeader().getSender();
    String deviceId = packet.getHeader().getDeviceId();
    channelHolder.removeSession(temail, deviceId);
    remoteStatusService.removeSession(temail, deviceId);
    CDTPLogoutResp.Builder builder = CDTPLogoutResp.newBuilder();
    builder.setCode(HttpStatus.OK.value());
    packet.setData(builder.build().toByteArray());
    channel.writeAndFlush(packet);

    if (channelHolder.hasNoSession(channel)) {
      channel.close();
    }
  }

  /**
   * 空闲或者异常退出
   *
   * @param channel 用户连接通道
   */
  public void terminateChannel(Channel channel) {
    List<Session> sessions = channelHolder.removeChannel(channel);
    remoteStatusService.removeSessions(sessions);
    channel.close();
  }

}