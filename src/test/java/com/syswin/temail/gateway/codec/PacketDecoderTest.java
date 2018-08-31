package com.syswin.temail.gateway.codec;

import static org.assertj.core.api.Assertions.assertThat;

import com.syswin.temail.gateway.client.PacketMaker;
import com.syswin.temail.gateway.entity.CDTPPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class PacketDecoderTest {

  private final ChannelHandlerContext context = null;
  private final String sender = "sender@t.email";
  private final String deviceId = "deviceId";

  private final List<Object> packets = new ArrayList<>();
  private final PacketDecoder decoder = new PacketDecoder();
  private final PacketEncoder encoder = new PacketEncoder();

  @Test
  public void shouldDecodeNormalPacketBytes() throws Exception {
    CDTPPacket packet = PacketMaker.loginPacket(sender, deviceId);
    ByteBuf buffer = Unpooled.buffer();
    // TODO(此处固定写长度，需要优化：结合LengthFieldPrepender等类进行处理)
    buffer.writeInt(169);
    encoder.encode(context, packet, buffer);
    decoder.decode(context, buffer, packets);

    assertThat(packets).isNotEmpty();
    assertThat(packets.get(0)).isEqualTo(packet);
  }

  @Test
  public void shouldDecodeSpecialPacketBytes() throws Exception {
    String message = "hello world";
    CDTPPacket packet = PacketMaker.singleChatPacket(sender, "recipient", message, deviceId);
    ByteBuf buffer = Unpooled.buffer();

    // TODO(此处固定写长度，需要优化：结合LengthFieldPrepender等类进行处理)
    buffer.writeInt(198);
    encoder.encode(context, packet, buffer);
    decoder.decode(context, buffer, packets);

    assertThat(packets).isNotEmpty();
    CDTPPacket actual = ((CDTPPacket) packets.get(0));
    assertThat(actual).isEqualToIgnoringGivenFields(packet, "data");

    // compare message in data field
    byte[] bytes = new byte[message.getBytes().length];
    System.arraycopy(actual.getData(), actual.getData().length - bytes.length, bytes, 0, bytes.length);
    assertThat(bytes).isEqualTo(packet.getData());

    packets.clear();
    buffer.clear();
    buffer.writeInt(198);

    // data field of decoded packet contains all packet bytes except length field
    buffer.writeBytes(actual.getData());

    decoder.decode(context, buffer, packets);

    assertThat(packets).isNotEmpty();
    assertThat(packets.get(0)).isEqualToIgnoringGivenFields(packet, "data");
  }
}
