package com.syswin.temail.ps.client;

import static com.syswin.temail.ps.client.CDTPClient.DEFAULT_EXECUTE_TIMEOUT;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.netty.channel.Channel;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.Setter;

/**
 * @author 姚华成
 * @date 2018-9-16
 */
class AsyncStub {

  private final List<RequestWrapper> requests;
  @Setter
  private Channel channel;

  AsyncStub(List<RequestWrapper> requests) {
    this.requests = requests;
  }

  public void execute(CDTPPacket reqPacket, Consumer<CDTPPacket> responseConsumer,
      Consumer<Throwable> errorConsumer) {
    execute(reqPacket, responseConsumer, errorConsumer, DEFAULT_EXECUTE_TIMEOUT, TimeUnit.SECONDS);
  }

  public void execute(CDTPPacket reqPacket, Consumer<CDTPPacket> responseConsumer,
      Consumer<Throwable> errorConsumer, long timeout, TimeUnit timeUnit) {
    synchronized (requests) {
      requests.add(new RequestWrapper(reqPacket, responseConsumer, errorConsumer, timeout, timeUnit));
    }
    channel.writeAndFlush(reqPacket);
  }

}
