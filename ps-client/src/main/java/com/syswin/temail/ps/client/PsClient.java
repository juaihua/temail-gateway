package com.syswin.temail.ps.client;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * PsClient的使用方法：
 * <pre>
 * private static PsClient psClient;
 * public void init() {
 *     if (psClient == null) {
 *         String deviceId = "deviceId";
 *         PsClientBuilder builder =
 *             new PsClientBuilder(deviceId)
 *                 .defaultHost("127.0.0.1")
 *                 .defaultPort(8099);
 *         psClient = builder.build();
 *     }
 * }
 *
 * public void sendMessage() {
 *     Message reqMessage = MessageMaker.sendSingleChatMessage(sender, receive, content);
 *     Message respMessage = psClient.sendMessage(reqMessage);
 * }
 * </pre>
 *
 * @author 姚华成
 * @date 2018-9-14
 */
public interface PsClient {

  /**
   * 登录服务器
   *
   * @param temail 需要登录的账号
   * @param temailPK 账号的公钥
   * @return 是否登录成功
   */
  boolean login(String temail, String temailPK);

  /**
   * 同步发送消息给服务端
   *
   * @param message 需要发送的消息对象
   * @return 服务器端响应的消息对象
   */
  Message sendMessage(Message message);

  /**
   * 同步发送消息给服务端
   *
   * @param message 需要发送的消息对象
   * @param timeout 超时时间
   * @param timeUnit 时间时间单位
   * @return 服务器端响应的消息对象
   */
  Message sendMessage(Message message, long timeout, TimeUnit timeUnit);

  /**
   * 异步发送消息给服务端
   *
   * @param message 需要发送的消息对象
   * @param responseConsumer 服务器端响应消息的处理器
   * @param errorConsumer 异常处理器
   */
  void sendMessage(Message message, Consumer<Message> responseConsumer, Consumer<Throwable> errorConsumer);

  /**
   * 异步发送消息给服务端
   *
   * @param message 需要发送的消息对象
   * @param responseConsumer 服务器端响应消息的处理器
   * @param errorConsumer 异常处理器
   * @param timeout 超时时间
   * @param timeUnit 时间时间单位
   */
  void sendMessage(Message message, Consumer<Message> responseConsumer, Consumer<Throwable> errorConsumer,
      long timeout, TimeUnit timeUnit);
}
