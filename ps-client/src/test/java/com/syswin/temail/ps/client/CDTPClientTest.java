package com.syswin.temail.ps.client;


import static com.syswin.temail.ps.client.PacketMaker.sendSingleCharRespPacket;
import static com.syswin.temail.ps.server.Constants.HTTP_STATUS_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.protobuf.InvalidProtocolBufferException;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLoginResp;
import com.syswin.temail.ps.server.connection.PsServer;
import com.syswin.temail.ps.server.service.AbstractSessionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
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
  private static TestRequestHandler testRequestHandler = Mockito.mock(TestRequestHandler.class);
  private static CDTPClient client;
  private String sender = "jack@t.email";
  private String receive = "sean@t.email";
  private String message = "hello world";

  @BeforeClass
  public static void startServer() {
    PsServer psServer =
        new PsServer(
            new AbstractSessionService() {
            },
            new TestRequestService(testRequestHandler));
    psServer.run(serverPort, serverReadIdleTimeSeconds);
  }

  @Before
  public void connect() {
    if (client == null) {
      String host = "localhost";
      client = new CDTPClient(host, serverPort, 30);
      client.connect();
    }
  }

  @Test
  public void syncExecute() throws InvalidProtocolBufferException {
    CDTPPacket reqPacket = PacketMaker.loginPacket(sender);
    CDTPPacket respPacket = client.syncExecute(reqPacket);
    CDTPLoginResp loginResp = CDTPLoginResp.parseFrom(respPacket.getData());
    assertThat(loginResp.getCode()).isEqualTo(HTTP_STATUS_OK);

    reqPacket = PacketMaker.sendSingleCharPacket(sender, receive, message);
    CDTPPacket respPacket1 = sendSingleCharRespPacket(reqPacket);
    when(testRequestHandler.dispatch(reqPacket)).thenReturn(respPacket1);
    respPacket = client.syncExecute(reqPacket);
    assertThat(respPacket).isEqualTo(respPacket1);
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
    AtomicReference<String> errorMsg = new AtomicReference<>();
    client.asyncExecute(loginReqPacket,
        respPacket -> {
          try {
            CDTPLoginResp loginResp = CDTPLoginResp.parseFrom(respPacket.getData());
            if (loginResp.getCode() != HTTP_STATUS_OK) {
              errorMsg.set("返回值不对，\n期望值：200，\n实际值：" + loginResp.getCode());
            }
          } catch (InvalidProtocolBufferException e) {
            errorMsg.set("返回值的data格式不对，" + new String(respPacket.getData()));
          }
          latch1.countDown();
        },
        throwable -> {
          errorMsg.set("请求异常");
          latch1.countDown();
        });
    latch1.await(3, TimeUnit.SECONDS);
    if (errorMsg.get() != null) {
      throw new Error(errorMsg.get());
    }
    CDTPPacket reqPacket = PacketMaker.sendSingleCharPacket(sender, receive, message);
    CDTPPacket exptectRespPacket = sendSingleCharRespPacket(reqPacket);
    when(testRequestHandler.dispatch(reqPacket)).thenReturn(exptectRespPacket);
    CountDownLatch latch2 = new CountDownLatch(1);
    errorMsg.set(null);
    client.asyncExecute(reqPacket,
        respPacket -> {
          if (!exptectRespPacket.equals(respPacket)) {
            errorMsg.set("返回值不对，\n期望值：" + exptectRespPacket + "\n实际值：" + respPacket);
          }
          latch2.countDown();
        },
        throwable -> {
          errorMsg.set("请求异常");
          latch2.countDown();
        });
    latch2.await(2, TimeUnit.SECONDS);
    if (errorMsg.get() != null) {
      throw new Error(errorMsg.get());
    }
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
      throw new Error(errorMsg.get());
    }
  }
}