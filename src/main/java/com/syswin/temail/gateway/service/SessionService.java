package com.syswin.temail.gateway.service;

import com.google.gson.Gson;
import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.Session;
import io.netty.channel.Channel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);

    HttpEntity<String> requestEntity = new HttpEntity<>(authDataJson, requestHeaders);

    ResponseEntity<Response> responseEntity = restTemplate
        .postForEntity(properties.getVerifyUrl(), requestEntity, Response.class);

    Response response = responseEntity.getBody();
    if (responseEntity.getStatusCode() == HttpStatus.OK) {
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
    // TODO(姚华成): 具体返回的内容需要跟前端商量
    packet.setData(gson.toJson(response).getBytes());
    channel.writeAndFlush(packet);
  }


  private void loginFailure(Channel channel, CDTPPacket packet, Response response) {
    // 登录失败返回错误消息，然后检查当前通道是否有登录用户，没有则关闭
    // TODO(姚华成): 具体返回内容需要跟前端核实确认
    packet.setData(gson.toJson(response).getBytes());
    channel.writeAndFlush(packet);
    List<Session> sessions = channelHolder.getSessions(channel);
    if (sessions.size() == 0) {
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
  }

  /**
   * 空闲或者异常退出
   *
   * @param channel 用户连接通道
   */
  public void channelTerminate(Channel channel) {
    List<Session> sessions = channelHolder.removeChannel(channel);
    remoteStatusService.removeSessions(sessions);
  }

}
