package com.syswin.temail.gateway.service;

import java.util.function.Consumer;

public interface DispatchService {

  void dispatch(byte[] data,
      Consumer<byte[]> consumer,
      Consumer<? super Throwable> errorConsumer);
}
