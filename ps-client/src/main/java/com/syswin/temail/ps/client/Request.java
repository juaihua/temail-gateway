package com.syswin.temail.ps.client;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.netty.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author 姚华成
 * @date 2018-9-16
 */
@Getter
@ToString
@RequiredArgsConstructor
class Request {

  private final CDTPPacket reqPacket;
  private final Consumer<CDTPPacket> responseConsumer;
  private Consumer<Throwable> errorConsumer;
  @Setter
  @Getter
  private ScheduledFuture<?> timeoutFuture;

  Request(CDTPPacket reqPacket,
      Consumer<CDTPPacket> responseConsumer, Consumer<Throwable> errorConsumer, long timeout,
      TimeUnit timeUnit) {
    this.reqPacket = reqPacket;
    this.responseConsumer = responseConsumer;
    this.errorConsumer = errorConsumer;
  }
}
