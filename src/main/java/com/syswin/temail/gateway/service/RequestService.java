package com.syswin.temail.gateway.service;

import static com.syswin.temail.gateway.entity.CommandSpaceType.CHANNEL;
import static com.syswin.temail.gateway.entity.CommandType.INTERNAL_ERROR;

import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPProtoBuf.CDTPServerError;
import io.netty.channel.Channel;
import javax.annotation.Resource;
import org.springframework.core.ParameterizedTypeReference;
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

    dispatcherWebClient.post()
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .syncBody(packet)
        .exchange()
        .subscribe(clientResponse -> clientResponse
            .bodyToMono(String.class)
            .subscribe(response -> {
              CDTPPacket respPacket;
              if (response != null) {
                // 后台正常返回
                respPacket = packet;
                respPacket.setData(response.getBytes());
              } else {
                respPacket = errorPacket(packet, INTERNAL_ERROR.getCode(), "dispatcher请求没有从服务器端返回结果对象：");
              }
              channel.writeAndFlush(respPacket);
            }),
            t -> {
              CDTPPacket respPacket = errorPacket(packet, INTERNAL_ERROR.getCode(), t.getMessage());
              channel.writeAndFlush(respPacket);
            }
        );
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
