package com.syswin.temail.ps.client;


import static com.syswin.temail.ps.client.PacketMaker.sendSingleCharRespPacket;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.server.connection.PsServer;
import com.syswin.temail.ps.server.service.AbstractSessionService;
import com.syswin.temail.ps.server.service.RequestService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author 姚华成
 * @date 2018-9-19
 */
public class CDTPClientTest {

  private static final int serverPort = 8099;
  private static final int serverReadIdleTimeSeconds = 300;
  private static RequestHandler requestHandler = Mockito.mock(RequestHandler.class);
  private CDTPClient client;
  private String sender = "jack@throwable.email";
  private String receive = "sean@throwable.email";
  private String message = "hello world";

  @BeforeClass
  public static void startServer() {
    PsServer psServer =
        new PsServer(
            new AbstractSessionService() {
            },
            new TestRequestService(requestHandler));
    psServer.run(serverPort, serverReadIdleTimeSeconds);
  }

  @Before
  public void connect() {
    if (client == null) {
      String host = "localhost";
      client = new CDTPClient(host, serverPort, 30, 1);
      client.connect();
    }
  }

  @Test
  public void syncExecute() throws InterruptedException {
    CDTPPacket reqPacket = PacketMaker.loginPacket(sender);
    CDTPPacket respPacket = client.syncExecute(reqPacket);
    assertThat(respPacket).isEqualToComparingFieldByField(reqPacket);

    reqPacket = PacketMaker.sendSingleCharPacket(sender, receive, message);
    CDTPPacket respPacket1 = sendSingleCharRespPacket(reqPacket);
    when(requestHandler.dispach(reqPacket)).thenReturn(respPacket1);
    respPacket = client.syncExecute(reqPacket);
    assertThat(respPacket).isEqualToComparingFieldByField(respPacket1);
//    Thread.sleep(3000);
  }

  @Test
  public void syncExecuteTimeout() {
    CDTPPacket reqPacket = PacketMaker.loginPacket(sender);
    CDTPPacket respPacket = client.syncExecute(reqPacket, 1, TimeUnit.NANOSECONDS);
    assertThat(respPacket).isNull();
  }

  @Test
  public void asyncExecute() throws InterruptedException {
    CDTPPacket loginReqPacket = PacketMaker.loginPacket(sender);
    CountDownLatch latch1 = new CountDownLatch(1);
    client.asyncExecute(loginReqPacket,
        respPacket -> {
          assertThat(respPacket).isEqualToComparingFieldByField(loginReqPacket);
          latch1.countDown();
        });
    latch1.await(3, TimeUnit.SECONDS);
    CDTPPacket reqPacket = PacketMaker.sendSingleCharPacket(sender, receive, message);
    CDTPPacket exptectRespPacket = sendSingleCharRespPacket(reqPacket);
    when(requestHandler.dispach(reqPacket)).thenReturn(exptectRespPacket);
    CountDownLatch latch2 = new CountDownLatch(1);
    client.asyncExecute(reqPacket,
        respPacket -> {
          assertThat(respPacket).isEqualToComparingFieldByField(exptectRespPacket);
          latch2.countDown();
        });
    latch2.await(2, TimeUnit.SECONDS);
  }

  @Test
  public void asyncExecuteTimeout() throws Throwable {
    CountDownLatch latch = new CountDownLatch(1);
    CDTPPacket reqPacket = PacketMaker.loginPacket(sender);
    AtomicReference<String> errorMsg = new AtomicReference<>();
    client.asyncExecute(reqPacket,
        respPacket -> {
          errorMsg.set("应该超时而没有超时！返回值：" + respPacket);
          latch.countDown();
        },
        t -> {
          if (!(t instanceof TimeoutException)) {
            errorMsg.set("非超时异常,异常信息：" + t);
          }
          latch.countDown();
        },
        1, TimeUnit.NANOSECONDS);
    latch.await(3, TimeUnit.SECONDS);
    if (errorMsg.get() != null) {
      throw new RuntimeException(errorMsg.get());
    }
  }

  private interface RequestHandler {

    default CDTPPacket dispach(CDTPPacket reqPacket) {
      return reqPacket;
    }
  }

  private static class TestRequestService implements RequestService {

    private RequestHandler handler;

    private TestRequestService(RequestHandler handler) {
      this.handler = handler;
    }

    @Override
    public void handleRequest(CDTPPacket reqPacket, Consumer<CDTPPacket> responseHandler) {
      responseHandler.accept(handler.dispach(reqPacket));
    }
  }

}