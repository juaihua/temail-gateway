package com.syswin.temail.ps.client;

import static com.syswin.temail.ps.common.entity.SignatureAlgorithm.ECC512_CODE;
import static com.syswin.temail.ps.server.Constants.HTTP_STATUS_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLoginResp;
import com.syswin.temail.ps.server.Constants;
import com.syswin.temail.ps.server.PsServer;
import com.syswin.temail.ps.server.service.AbstractSessionService;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * @author 姚华成
 * @date 2018-9-17
 */
@Slf4j
public class PsClientTest {

  private static final int serverPort = 8098;
  private static final int serverReadIdleTimeSeconds = 300;
  private static TestRequestHandler testRequestHandler = Mockito.mock(TestRequestHandler.class);
  private static String sender = "jack@t.email";
  private static String receive = "sean@t.email";
  private static String content = "hello world";
  private static List<String> validUsers = Arrays.asList(sender, receive);
  private static PsClient psClient;

  @BeforeClass
  public static void startServer() {
    PsServer psServer =
        new PsServer(
            new TestSessionService(),
            new TestRequestService(testRequestHandler),
            serverPort, serverReadIdleTimeSeconds);
    psServer.run();
  }

  @Before
  public void init() {
    if (psClient == null) {
      Signer signer = Mockito.mock(Signer.class);
      when(signer.getAlgorithm()).thenReturn(ECC512_CODE);
      when(signer.sign(any(), any())).thenReturn("Signed Data");

      String deviceId = "deviceId";
      PsClientBuilder builder =
          new PsClientBuilder(deviceId)
              .defaultHost("127.0.0.1")
              .defaultPort(serverPort)
              .signer(signer);
      psClient = builder.build();
    }
  }

  @Test
  public void sendMessage() throws InterruptedException {
    Message reqMessage = MessageMaker.sendSingleChatMessage(sender, receive, content);
    CDTPPacket packet = MessageConverter.toCDTPPacket(reqMessage);
    mockDispatch(packet);
    Message respMessage = psClient.sendMessage(reqMessage);
    assertThat(respMessage).isEqualTo(reqMessage);
    Thread.sleep(100);
  }

  @Test
  public void sendMessageTimeout() throws InterruptedException {
    Message reqMessage = MessageMaker.sendSingleChatMessage(sender, receive, content);
    CDTPPacket packet = MessageConverter.toCDTPPacket(reqMessage);
    mockDispatch(packet);
    Message respMessage = psClient.sendMessage(reqMessage, 1, TimeUnit.NANOSECONDS);
    assertThat(respMessage).isNull();
    Thread.sleep(100);
  }


  @Test(expected = PsClientException.class)
  public void sendMessageInvalidUser() throws InterruptedException {
    // 先通过发送一个正常消息，建立并保持连接，以避免服务器通道中只有一个无效用户而把通道断开
    sendMessage();
    Message reqMessage = MessageMaker.sendSingleChatMessage("invalidUser", receive, content);
    psClient.sendMessage(reqMessage);
    Thread.sleep(100);
  }

  @Test
  public void sendMessageAsync() throws InterruptedException {
    Message reqMessage = MessageMaker.sendSingleChatMessage(sender, receive, content);
    CDTPPacket packet = MessageConverter.toCDTPPacket(reqMessage);
    mockDispatch(packet);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<String> errorMsg = new AtomicReference<>();
    psClient.sendMessage(reqMessage, message -> {
      if (!message.equals(reqMessage)) {
        errorMsg.set("返回对象不是期望的对象,\n返回值：" + message + "\n期望值：" + reqMessage);
      }
      latch.countDown();
    }, t -> {
      errorMsg.set("调用错误：" + Arrays.toString(t.getStackTrace()));
      latch.countDown();
    });
    latch.await(3, TimeUnit.SECONDS);
    if (errorMsg.get() != null) {
      throw new Error(errorMsg.get());
    }
    Thread.sleep(100);
  }

  private void mockDispatch(CDTPPacket packet) {
    reset(testRequestHandler);
    when(testRequestHandler.dispatch(ArgumentMatchers.argThat(
        argument -> {
          String argPacketId = argument.getHeader().getPacketId();
          String packetId = packet.getHeader().getPacketId();
          return argPacketId.equals(packetId);
        })))
        .thenReturn(packet);
  }

  private static class TestSessionService extends AbstractSessionService {

    @Override
    protected boolean loginExt(CDTPPacket reqPacket, CDTPPacket respPacket) {
      String temail = reqPacket.getHeader().getSender();
      CDTPLoginResp.Builder builder = CDTPLoginResp.newBuilder();
      if (validUsers.contains(temail)) {
        builder.setCode(HTTP_STATUS_OK);
        respPacket.setData(builder.build().toByteArray());
        return true;
      } else {
        builder.setCode(Constants.HTTP_STATUS_NOT_FOUND);
        builder.setDesc("用户:" + reqPacket.getHeader().getSender() + "不存在！");
        respPacket.setData(builder.build().toByteArray());
        return false;
      }
    }
  }
}