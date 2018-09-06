package com.syswin.temail.gateway.service;

import static com.syswin.temail.gateway.entity.CommandSpaceType.CHANNEL;
import static com.syswin.temail.gateway.entity.CommandType.INTERNAL_ERROR;

import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPPacketTrans;
import com.syswin.temail.gateway.entity.CDTPProtoBuf.CDTPServerError;
import io.netty.channel.Channel;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class RequestService {

  private final DispatchService dispatchService;
  @Resource
  private ChannelHolder channelHolder;
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
      errorPacket(packet, INTERNAL_ERROR.getCode(), "用户" + temail + "在设备" + deviceId + "上没有登录，无法进行操作！");

      channel.writeAndFlush(packet);
      return;
    }

    dispatchService.dispatch(properties.getDispatchUrl(), new CDTPPacketTrans(packet),
        clientResponse -> clientResponse
            .bodyToMono(String.class)
            .subscribe(response -> {
              CDTPPacket respPacket;
              if (response != null) {
                // 后台正常返回
                respPacket = packet;
                respPacket.setData(response.getBytes());
              } else {
                respPacket =
                    errorPacket(packet, INTERNAL_ERROR.getCode(), "dispatcher请求没有从服务器端返回结果对象：");
              }
              // 请求的数据可能加密，而返回的数据没有加密，需要设置加密标识
              respPacket.getHeader().setDataEncryptionMethod(0);
              channel.writeAndFlush(respPacket);
            }), t -> channel.writeAndFlush(
            errorPacket(packet, INTERNAL_ERROR.getCode(), t.getMessage())));
  }

  private CDTPPacket errorPacket(CDTPPacket packet, int code, String message) {
    packet.setCommandSpace(CHANNEL.getCode());
    packet.setCommand(INTERNAL_ERROR.getCode());

    CDTPServerError.Builder builder = CDTPServerError.newBuilder();
    builder.setCode(code);
    builder.setDesc(message);
    packet.setData(builder.build().toByteArray());
    return packet;
  }

  private boolean authSession(Channel channel, String temail, String deviceId) {
    return channel == channelHolder.getChannel(temail, deviceId);
  }
}
