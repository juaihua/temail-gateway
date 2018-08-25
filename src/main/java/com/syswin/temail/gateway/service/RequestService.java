package com.syswin.temail.gateway.service;

import com.syswin.temail.gateway.entity.CDTPPacket;
import io.netty.channel.Channel;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * @author 姚华成
 * @date 2018-8-24
 */
@Service
public class RequestService {

  @Resource(name = "dispatcherWebClient")
  private WebClient webClient;
  @Resource
  private ChannelHolder channelHolder;

  public void handleRequest(Channel channel, CDTPPacket packet) {
    if (!authSession(channel, packet)) {
      // TODO(姚华成):构建错误的返回值
      channel.writeAndFlush(packet);
      return;
    }

    Mono<ClientResponse> mono = webClient.post()
        .header("", "")
        .body(null)
        .exchange();
//    mono.
  }

  private boolean authSession(Channel channel, CDTPPacket packet) {
    String temail = packet.getHeader().getSender();
    String deviceId = packet.getHeader().getDeviceId();
    return channel == channelHolder.getChannel(temail, deviceId);
  }
}
