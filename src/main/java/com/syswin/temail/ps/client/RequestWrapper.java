package com.syswin.temail.ps.client;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author 姚华成
 * @date 2018-9-16
 */
@Getter
@RequiredArgsConstructor
class RequestWrapper {

  private final CDTPPacket reqPacket;
  private final Consumer<CDTPPacket> responseConsumer;
  private Consumer<Throwable> errorConsumer;
  private long timeoutInMillis;
  private long startTime;

  RequestWrapper(CDTPPacket reqPacket,
      Consumer<CDTPPacket> responseConsumer, Consumer<Throwable> errorConsumer, long timeout,
      TimeUnit timeUnit) {
    this.reqPacket = reqPacket;
    this.responseConsumer = responseConsumer;
    this.errorConsumer = errorConsumer;
    this.timeoutInMillis = timeUnit.toMillis(timeout);
    this.startTime = System.currentTimeMillis();
  }

  boolean matchResponse(CDTPPacket respPacket) {
    try {
      if (respPacket.getCommandSpace() == CHANNEL.getCode()) {
        switch (respPacket.getCommand()) {
          case 101:
          case 102:
            return reqPacket.getHeader().getSender().equals(respPacket.getHeader().getSender());
          case 600:
            return false;
          default:
            return false;
        }
      }
      String reqPacketId = reqPacket.getHeader().getPacketId();
      String respPacketId = respPacket.getHeader().getPacketId();
      return reqPacket.getCommandSpace() == respPacket.getCommandSpace() &&
          reqPacket.getCommand() == respPacket.getCommand() &&
          reqPacketId.equals(respPacketId);
    } catch (Exception e) {
      return false;
    }
  }

  boolean hasTimeout() {
    return timeoutInMillis > 0 && (System.currentTimeMillis() - startTime) >= timeoutInMillis;
  }
}
