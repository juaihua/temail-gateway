package com.syswin.temail.cdtpserver.client;

import static com.syswin.temail.gateway.Constants.CDTP_VERSION;
import static com.syswin.temail.gateway.entity.CommandSpaceType.CHANNEL;
import static com.syswin.temail.gateway.entity.CommandType.LOGIN;

import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPPacket.Header;
import io.netty.channel.Channel;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author 姚华成
 * @date 2018-8-25
 */
@RunWith(SpringRunner.class)
public class SessionCommandTest {

  private static Channel channel;

  @BeforeClass
  public static void init() {
    String host = "127.0.0.1";
    int port = 8099;
    channel = NettyClient.startClient(host, port);
  }

  @Test
  public void testLogin() {
    Header header = new Header("deviceId1", 1, "signature", 0, System.currentTimeMillis(), "packetId",
        "sender@syswin.com", "senderPK", "receiver@syswin.com", "receiverPK", "at", "topic", "extraData");
    byte[] data = new byte[0];
    CDTPPacket packet = new CDTPPacket(CHANNEL.getCode(), LOGIN.getCode(), CDTP_VERSION, header, data);
    channel.writeAndFlush(packet);
    channel.closeFuture().syncUninterruptibly();
  }

}
