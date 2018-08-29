package com.syswin.temail.gateway.service;

import static com.syswin.temail.gateway.entity.CommandSpaceType.CHANNEL;
import static com.syswin.temail.gateway.entity.CommandType.INTERNAL_ERROR;

import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPProtoBuf.CDTPServerError;
import com.syswin.temail.gateway.entity.Response;
import io.netty.channel.Channel;
import javax.annotation.Resource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author 姚华成
 * @date 2018-8-24
 */
@Service
public class RequestService {

  @Resource(name = "dispatcherWebClient")
  private WebClient dispatcherWebClient;
  @Resource
  private ChannelHolder channelHolder;

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
        .syncBody(packet)
        .exchange()
        .subscribe(clientResponse -> clientResponse
                .bodyToMono(new ParameterizedTypeReference<Response<CDTPPacket>>() {
                }).subscribe(response -> {
                  CDTPPacket respPacket;
                  if (response != null && response.getData() != null) {
                    // 后台正常返回
                    respPacket = response.getData();
                  } else {
                    if (response == null) {
                      respPacket = errorPacket(packet, INTERNAL_ERROR.getCode(), "dispatcher请求没有从服务器端返回结果对象：");
                    } else {
                      respPacket = errorPacket(packet, response.getCode(), response.getMessage());
                    }
                  }
                  channel.writeAndFlush(respPacket);
                }),
            t -> {
              CDTPPacket respPacket = errorPacket(packet, INTERNAL_ERROR.getCode(), t.getMessage());
              channel.writeAndFlush(respPacket);
            });
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
