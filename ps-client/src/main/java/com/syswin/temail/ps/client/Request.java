package com.syswin.temail.ps.client;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.netty.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
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

  @NonNull
  private final CDTPPacket reqPacket;
  @NonNull
  private final Consumer<CDTPPacket> responseConsumer;
  @NonNull
  private final Consumer<Throwable> errorConsumer;
  @Setter
  @Getter
  private ScheduledFuture<?> timeoutFuture;
}
