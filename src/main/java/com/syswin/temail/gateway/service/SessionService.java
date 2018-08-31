package com.syswin.temail.gateway.service;


import javax.annotation.Resource;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
public class SessionService {

  private final LoginService loginService;
  @Resource
  private ChannelHolder channelHolder;
  @Resource
  private RemoteStatusService remoteStatusService;

  @Autowired
  public SessionService(RestTemplate restTemplate, TemailGatewayProperties properties) {
    loginService = new LoginService(restTemplate, properties.getVerifyUrl());
  }

  public void login(Channel channel, CDTPPacket packet) {
    String temail = packet.getHeader().getSender();
    String deviceId = packet.getHeader().getDeviceId();
    if (!StringUtils.hasText(temail) || !StringUtils.hasText(deviceId)) {
      throw new PacketException("登录时temail和deviceId不可以为空！", packet);
    }

    try {
      CDTPLogin cdtpLogin = CDTPLogin.parseFrom(packet.getData());
    } catch (InvalidProtocolBufferException e) {
      throw new PacketException(e, packet);
    }
    // TODO(姚华成): 这个cdtpLogin对象暂时没用，后续根据业务需要再完善？
    // TODO 当前认证请求做简化处理，未来需要完善
    ResponseEntity<Response> responseEntity = loginService.login(temail, "", "");
    Response response = responseEntity.getBody();

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
    remoteStatusService.addSession(temail, deviceId, null);
    // 返回成功的消息
    CDTPLoginResp.Builder builder = CDTPLoginResp.newBuilder();
    builder.setCode(response == null ? HttpStatus.OK.value() : response.getCode());
    if (response != null && response.getMessage() != null) {
      builder.setDesc(response.getMessage());
    }
    packet.setData(builder.build().toByteArray());
    channel.writeAndFlush(packet);
  }

  private void loginFailure(Channel channel, CDTPPacket packet, Response response) {
    // 登录失败返回错误消息，然后检查当前通道是否有登录用户，没有则关闭
    CDTPLoginResp.Builder builder = CDTPLoginResp.newBuilder();
    if (response != null) {
      if (response.getCode() != null) {
        builder.setCode(response.getCode());
      } else {
        builder.setCode(HttpStatus.FORBIDDEN.value());
      }
      if (response.getMessage() != null) {
        builder.setDesc(response.getMessage());
      }
    } else {
      builder.setCode(HttpStatus.FORBIDDEN.value());
    }
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
    remoteStatusService.removeSession(temail, deviceId,null);
    CDTPLogoutResp.Builder builder = CDTPLogoutResp.newBuilder();
    builder.setCode(HttpStatus.OK.value());
    packet.setData(builder.build().toByteArray());
    channel.writeAndFlush(packet);

    channelHolder.removeSession(temail, deviceId, channel);
  }

  /**
   * 空闲或者异常退出
   *
   * @param channel 用户连接通道
   */
  public void terminateChannel(Channel channel) {
    Iterable<Session> sessions = channelHolder.removeChannel(channel);
    remoteStatusService.removeSessions(sessions,null);
  }

}
