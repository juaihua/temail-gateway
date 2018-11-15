package com.syswin.temail.gateway.client;

import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.syswin.temail.gateway.codec.FullPacketAwareDecoder;
import com.syswin.temail.gateway.codec.RawPacketDecoder;
import com.syswin.temail.gateway.codec.RawPacketEncoder;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.exception.PacketException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class RawPacketCodecTest {

  private final ChannelId channelId = Mockito.mock(ChannelId.class);
  private final Channel channel = Mockito.mock(Channel.class);
  private final ChannelHandlerContext context = Mockito.mock(ChannelHandlerContext.class);
  private final String sender = "sender@t.email";
  private final String deviceId = uniquify("deviceId");

  private final List<Object> packets = new ArrayList<>();
  private final RawPacketDecoder decoder = new FullPacketAwareDecoder();
  private final RawPacketEncoder encoder = new RawPacketEncoder();
  private final CDTPPacket packet = PacketMaker.singleChatPacket(sender, "recipient", "hello world", deviceId);
  private final ByteBuf buffer = Unpooled.buffer();

  @Before
  public void setUp() {
    when(context.channel()).thenReturn(channel);
    when(channel.id()).thenReturn(channelId);
  }

  @Test
  public void shouldDecodePacketWithFullPayloadBytes() {
    ByteBuf bufferIncludeLength = Unpooled.buffer();

    encoder.encode(context, packet, buffer);
    bufferIncludeLength.writeInt(buffer.readableBytes());
    bufferIncludeLength.writeBytes(buffer.retain());

    decoder.decode(context, bufferIncludeLength, packets);

    assertThat(packets).isNotEmpty();
    CDTPPacket decodedPacket = ((CDTPPacket) packets.get(0));
    assertThat(decodedPacket.getCommandSpace()).isEqualTo(packet.getCommandSpace());
    assertThat(decodedPacket.getCommand()).isEqualTo(packet.getCommand());
    assertThat(decodedPacket.getHeader().getDeviceId()).isEqualTo(packet.getHeader().getDeviceId());
    assertThat(decodedPacket.getHeader().getSender()).isEqualTo(packet.getHeader().getSender());

    packets.clear();
    buffer.clear();

    // data contains full packet payload
    buffer.writeBytes(decodedPacket.getData());

    decoder.decode(context, buffer, packets);

    assertThat(packets).isNotEmpty();
    assertThat(packets.get(0)).isEqualToIgnoringGivenFields(packet, "data");
  }

  @Test(expected = PacketException.class)
  public void blowsUpIfNegativePacketLength() {
    ByteBuf buffer = Unpooled.buffer();
    buffer.writeInt(-1);
    encoder.encode(context, packet, buffer);

    decoder.decode(context, buffer, packets);
  }
}
