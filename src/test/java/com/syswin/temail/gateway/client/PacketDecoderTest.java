package com.syswin.temail.gateway.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.codec.CommandAwareBodyExtractor;
import com.syswin.temail.ps.common.codec.PacketDecoder;
import com.syswin.temail.ps.common.codec.PacketEncoder;
import com.syswin.temail.ps.common.codec.SimpleBodyExtractor;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.mockito.Mockito;

public class PacketDecoderTest {

  private final ChannelHandlerContext context = Mockito.mock(ChannelHandlerContext.class);
  private final String sender = "sender@t.email";
  private final String deviceId = "deviceId";

  private final List<Object> packets = new ArrayList<>();
  private final PacketDecoder decoder = new PacketDecoder(new CommandAwareBodyExtractor(new SimpleBodyExtractor(),
      new TemailGatewayProperties()));
  private final PacketEncoder encoder = new PacketEncoder();

  @Test
  public void shouldDecodeNormalPacketBytes() {
    CDTPPacket packet = PacketMaker.loginPacket(sender, deviceId);
    ByteBuf buffer = Unpooled.buffer();
    ByteBuf bufferIncludeLength = Unpooled.buffer();

    encoder.encode(context, packet, buffer);
    bufferIncludeLength.writeInt(buffer.readableBytes());
    bufferIncludeLength.writeBytes(buffer.retain());

    decoder.decode(context, bufferIncludeLength, packets);

    assertThat(packets).isNotEmpty();
    assertThat(packets.get(0)).isEqualTo(packet);
  }

  @Test
  public void shouldDecodeSpecialPacketBytes() {
    String message = "hello world";
    CDTPPacket packet = PacketMaker.singleChatPacket(sender, "recipient", message, deviceId);
    ByteBuf buffer = Unpooled.buffer();
    ByteBuf bufferIncludeLength = Unpooled.buffer();

    encoder.encode(context, packet, buffer);
    bufferIncludeLength.writeInt(buffer.readableBytes());
    bufferIncludeLength.writeBytes(buffer.retain());

    decoder.decode(context, bufferIncludeLength, packets);

    assertThat(packets).isNotEmpty();
    CDTPPacket actual = ((CDTPPacket) packets.get(0));
    assertThat(actual).isEqualToIgnoringGivenFields(packet, "data");

    // compare message in data field
    byte[] bytes = new byte[message.getBytes().length];
    System.arraycopy(actual.getData(), actual.getData().length - bytes.length, bytes, 0, bytes.length);
    assertThat(bytes).isEqualTo(packet.getData());

    packets.clear();
    buffer.clear();

    // 经过修改以后，单聊的Data中的CDTP数据已经包含了长度
    buffer.writeBytes(actual.getData());

    decoder.decode(context, buffer, packets);

    assertThat(packets).isNotEmpty();
    assertThat(packets.get(0)).isEqualToIgnoringGivenFields(packet, "data");
  }
}
