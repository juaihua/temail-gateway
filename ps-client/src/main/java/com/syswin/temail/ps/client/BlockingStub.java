package com.syswin.temail.ps.client;

import static com.syswin.temail.ps.client.CDTPClient.DEFAULT_EXECUTE_TIMEOUT;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.netty.channel.Channel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.Setter;

/**
 * @author 姚华成
 * @date 2018-9-16
 */
class BlockingStub {

  private final ConcurrentHashMap<String, RequestWrapper> requestMap;
  @Setter
  private Channel channel;

  BlockingStub(ConcurrentHashMap<String, RequestWrapper> requestMap) {
    this.requestMap = requestMap;
  }

  public CDTPPacket execute(CDTPPacket reqPacket) {
    return execute(reqPacket, DEFAULT_EXECUTE_TIMEOUT, TimeUnit.SECONDS);
  }

  public CDTPPacket execute(CDTPPacket reqPacket, long timeout, TimeUnit timeUnit) {
    try {
      String packetId = reqPacket.getHeader()
          .getPacketId();
      CountDownLatch latch = new CountDownLatch(1);
      ResponseWrapper responseWrapper = new ResponseWrapper();
      RequestWrapper requestWrapper = new RequestWrapper(reqPacket, packet -> {
        responseWrapper.response = packet;
        requestMap.remove(packetId);
        latch.countDown();
      });
      requestMap.put(packetId, requestWrapper);
      channel.writeAndFlush(reqPacket);
      latch.await(timeout, timeUnit);
      return responseWrapper.response;
    } catch (InterruptedException e) {
      throw new PsClientException("执行CDTP请求出错", e);
    }
  }

}
