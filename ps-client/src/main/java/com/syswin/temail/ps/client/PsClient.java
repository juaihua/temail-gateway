package com.syswin.temail.ps.client;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author 姚华成
 * @date 2018-9-14
 */
public interface PsClient {

  Message sendMessage(Message message);

  Message sendMessage(Message message, long timeout, TimeUnit timeUnit);

  void sendMessage(Message message, Consumer<Message> responseConsumer, Consumer<Throwable> errorConsumer);

  void sendMessage(Message message, Consumer<Message> responseConsumer, Consumer<Throwable> errorConsumer,
      long timeout, TimeUnit timeUnit);
}
