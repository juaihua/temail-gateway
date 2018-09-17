package com.syswin.temail.ps.client;

import static com.syswin.temail.ps.client.CDTPClient.DEFAULT_EXECUTE_TIMEOUT;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.netty.channel.Channel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.Setter;

/**
 * @author 姚华成
 * @date 2018-9-16
 */
class AsyncStub {

  private final ConcurrentHashMap<String, RequestWrapper> requestMap;
  @Setter
  private Channel channel;

  AsyncStub(ConcurrentHashMap<String, RequestWrapper> requestMap) {
    this.requestMap = requestMap;
  }

  public void execute(CDTPPacket reqPacket, Consumer<CDTPPacket> responseConsumer,
      Consumer<Throwable> errorConsumer) {
    execute(reqPacket, responseConsumer, errorConsumer, DEFAULT_EXECUTE_TIMEOUT, TimeUnit.SECONDS);
  }

  public void execute(CDTPPacket reqPacket, Consumer<CDTPPacket> responseConsumer,
      Consumer<Throwable> errorConsumer, long timeout, TimeUnit timeUnit) {
    String packetId = reqPacket.getHeader()
        .getPacketId();
    requestMap.put(packetId,
        new RequestWrapper(reqPacket, responseConsumer, errorConsumer, timeout, timeUnit));
    channel.writeAndFlush(reqPacket);
  }

}
