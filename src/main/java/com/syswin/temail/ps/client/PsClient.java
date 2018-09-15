package com.syswin.temail.ps.client;

import java.util.function.Consumer;

/**
 * @author 姚华成
 * @date 2018-9-14
 */
public interface PsClient {

  Message sendMessage(Message message);

  void sendMessage(Message message, Consumer<Message> responseConsumer, Consumer<Message> errorConsumer);
}
