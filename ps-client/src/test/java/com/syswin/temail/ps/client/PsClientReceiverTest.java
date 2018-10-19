package com.syswin.temail.ps.client;

import static com.syswin.temail.ps.common.entity.SignatureAlgorithm.ECC512_CODE;
import static com.syswin.temail.ps.server.Constants.HTTP_STATUS_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLoginResp;
import com.syswin.temail.ps.server.Constants;
import com.syswin.temail.ps.server.PsServer;
import com.syswin.temail.ps.server.service.AbstractSessionService;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author 姚华成
 * @date 2018-10-17
 */
public class PsClientReceiverTest {

  private static final int serverPort = 10102;
  private static final int serverReadIdleTimeSeconds = 300;
  private static String sender = "jack@t.email";
  private static String receive = "sean@t.email";
  private static String content = "hello world";
  private static List<String> validUsers = Arrays.asList(sender, receive);
  private static PsClient psClient;

  private static TestRequestHandler testRequestHandler = new TestRequestHandler() {
    @Override
    public CDTPPacket dispatch(CDTPPacket reqPacket) {
      CDTPPacket respPacket = new CDTPPacket(reqPacket);
      respPacket.getHeader().setPacketId(UUID.randomUUID().toString());
      return respPacket;
    }
  };

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
  public void testReceiver() throws InterruptedException {
    Message reqMessage = MessageMaker.sendSingleChatMessage(sender, receive, content);

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Message> receiveMessage = new AtomicReference<>();
    psClient.login(receive, "receiverPK", message -> {
      receiveMessage.set(message);
      latch.countDown();
    });
    psClient.sendMessage(reqMessage,
        message -> {
        },
        throwable -> {
        });
    latch.await();
    assertThat(receiveMessage.get().getPayload()).isEqualTo(reqMessage.getPayload());
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
