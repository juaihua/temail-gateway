package com.syswin.temail.gateway.service;

import static com.syswin.temail.gateway.entity.CommandSpaceType.CHANNEL;
import static com.syswin.temail.gateway.entity.CommandType.INTERNAL_ERROR;

import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPProtoBuf.CDTPServerError;
import com.syswin.temail.gateway.entity.Response;
import io.netty.channel.Channel;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class RequestService {

  @Resource
  private ChannelHolder channelHolder;

  private final DispatchService dispatchService;

  @Resource
  private TemailGatewayProperties properties;

  @Autowired
  public RequestService(WebClient dispatcherWebClient) {
    dispatchService = new DispatchService(dispatcherWebClient);
  }


  public void handleRequest(Channel channel, CDTPPacket packet) {
    String temail = packet.getHeader().getSender();
    String deviceId = packet.getHeader().getDeviceId();
    if (!authSession(channel, temail, deviceId)) {
      packet.setCommandSpace(CHANNEL.getCode());
      packet.setCommand(INTERNAL_ERROR.getCode());

      CDTPServerError.Builder builder = CDTPServerError.newBuilder();
      builder.setCode(INTERNAL_ERROR.getCode());
      builder.setDesc("用户" + temail + "在设备" + deviceId + "上没有登录，无法进行操作！");
      packet.setData(builder.build().toByteArray());

      channel.writeAndFlush(packet);
      return;
    }

    dispatchService.dispatch(properties.getDispatchUrl(), packet, new DispatchCallback() {

      @Override
      public void onSuccess(Response response) {
        CDTPPacket respPacket;
        if (response != null && response.getData() != null) {
          // 后台正常返回
          respPacket = (CDTPPacket) response.getData();
        } else {
          int code;
          String message;
          if (response == null) {
            code = INTERNAL_ERROR.getCode();
            message = "dispatcher请求没有从服务器端返回结果对象：";
          } else {
            code = response.getCode();
            message = response.getMessage();
          }
          respPacket = errorPacket(packet, code, message);
        }
        channel.writeAndFlush(respPacket);
      }

      @Override
      public void onError(Throwable throwable) {
        CDTPPacket respPacket = errorPacket(packet, INTERNAL_ERROR.getCode(), throwable.getMessage());
        channel.writeAndFlush(respPacket);
      }

    });
  }

  private CDTPPacket errorPacket(CDTPPacket packet, int code, String message) {
    CDTPPacket respPacket = packet;
    respPacket.setCommandSpace(CHANNEL.getCode());
    respPacket.setCommand(INTERNAL_ERROR.getCode());

    CDTPServerError.Builder builder = CDTPServerError.newBuilder();
    builder.setCode(code);
    builder.setDesc(message);
    respPacket.setData(builder.build().toByteArray());
    return respPacket;
  }

  private boolean authSession(Channel channel, String temail, String deviceId) {
    return channel == channelHolder.getChannel(temail, deviceId);
  }

}
