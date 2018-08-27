package com.syswin.temail.gateway.service;

import com.syswin.temail.gateway.entity.CDTPPacket;
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
    if (!authSession(channel, packet)) {
      // TODO(姚华成):构建错误的返回值
      channel.writeAndFlush(packet);
      return;
    }

    dispatcherWebClient.post()
        .header("", "")
        .body(null)
        .exchange()
        .doOnSuccess(clientResponse -> {
          Response<CDTPPacket> response = clientResponse
              .bodyToMono(new ParameterizedTypeReference<Response<CDTPPacket>>() {
              }).block();
          if (response != null) {
            CDTPPacket respPacket = response.getData();
            packet.setData(respPacket.getData());
            channel.writeAndFlush(packet);
          } else {
            // TODO(姚华成): 返回错误信息
            channel.writeAndFlush(packet);
          }
        })
        .subscribe()
    ;
  }

  private boolean authSession(Channel channel, CDTPPacket packet) {
    String temail = packet.getHeader().getSender();
    String deviceId = packet.getHeader().getDeviceId();
    return channel == channelHolder.getChannel(temail, deviceId);
  }

}
