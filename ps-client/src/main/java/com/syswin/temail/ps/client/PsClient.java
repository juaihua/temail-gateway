package com.syswin.temail.ps.client;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.utils.PacketUtil;
import java.util.Base64;
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
   * 建立与ps-server的连接<br> 由于登录、发送消息等能够自动建立连接，因此手工建立连接不是必要的
   */
  void connect();

  /**
   * 建立与ps-server的连接<br> 由于登录、发送消息等能够自动建立连接，因此手工建立连接不是必要的
   *
   * @param targetAddress 指定连接的服务器地址
   */
  void connect(String targetAddress);

  /**
   * 关闭与默认ps-server的连接
   */
  void disconnect();

  /**
   * 关闭与指定ps-server的连接
   *
   * @param targetAddress 指定断开连接的服务器地址
   */
  void disconnect(String targetAddress);

  /**
   * 登录服务器
   *
   * @param temail 需要登录的账号
   * @param temailPK 账号的公钥
   * @param receiveCallback 接收消息的回调方法
   * @throws PsClientException 登录不成功，以异常的方式返回。具体原因在异常里描述
   */
  void login(String temail, String temailPK, Consumer<Message> receiveCallback);

  /**
   * 登录指定服务器
   *
   * @param temail 需要登录的账号
   * @param temailPK 账号的公钥
   * @param targetAddress 指定登出的服务器地址
   * @param receiveCallback 接收消息的回调方法
   * @throws PsClientException 登录不成功，以异常的方式返回。具体原因在异常里描述
   */
  void login(String temail, String temailPK, String targetAddress, Consumer<Message> receiveCallback);

  /**
   * 登出服务器<br> 不能再接收消息，不影响发送消息
   *
   * @param temail 需要登录的账号
   * @param temailPK 账号的公钥
   * @throws PsClientException 登录不成功，以异常的方式返回。具体原因在异常里描述
   */
  void logout(String temail, String temailPK);

  /**
   * 登出服务器<br> 不能再接收消息，不影响发送消息
   *
   * @param temail 需要登录的账号
   * @param temailPK 账号的公钥
   * @param targetAddress 指定登录的服务器地址
   * @throws PsClientException 登出不成功，以异常的方式返回。具体原因在异常里描述
   */
  void logout(String temail, String temailPK, String targetAddress);

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

  /**
   * 将收到的字节数组的CDTPPacket进行解包，生成Message对象。主要用于单聊、群聊等服务端保留完整Packet的情况
   *
   * @param packetData Base64UrlSafe形式的CDTPPacket包，包含前导的长度
   * @return 解包后的Message对象
   */
  static Message unpack(String packetData) {
    return unpack(Base64.getUrlDecoder().decode(packetData));
  }

  /**
   * 将收到的字节数组的CDTPPacket进行解包，生成Message对象。主要用于单聊、群聊等服务端保留完整Packet的情况
   *
   * @param packetData 字节数组形式的CDTPPacket包，包含前导的长度
   * @return 解包后的Message对象
   */
  static Message unpack(byte[] packetData) {
    CDTPPacket packet = PacketUtil.unpack(packetData);
    return MessageConverter.fromCDTPPacket(packet);
  }

  static byte[] pack(Message message) {
    CDTPPacket packet = MessageConverter.toCDTPPacket(message);
    return PacketUtil.pack(packet);
  }
}
