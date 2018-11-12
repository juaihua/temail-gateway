package com.syswin.temail.gateway.service;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.function.Consumer;

public interface DispatchService {

  void dispatch(CDTPPacket packet,
      Consumer<byte[]> consumer,
      Consumer<? super Throwable> errorConsumer);
}
