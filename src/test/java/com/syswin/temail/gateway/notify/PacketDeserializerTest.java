package com.syswin.temail.gateway.notify;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syswin.temail.gateway.client.PacketMaker;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import org.junit.Test;

public class PacketDeserializerTest {

  private final CDTPPacketTrans packetTrans = PacketMaker.mqMsgPayload("sean@t.email", "hello");

  private final Gson gson = new GsonBuilder()
      .registerTypeAdapter(CDTPPacket.class, new PacketDeserializer())
      .create();

  @Test
  public void shouldDeserializeDataAsBytes() {
    String json = gson.toJson(packetTrans);

    CDTPPacket packet = gson.fromJson(json, CDTPPacket.class);

    assertThat(packet).isEqualToIgnoringGivenFields(packetTrans, "data");
    assertThat(new String(packet.getData())).isEqualTo(packetTrans.getData());
  }

  @Test
  public void ignoreNullField() {
    packetTrans.setData(null);
    String json = gson.toJson(packetTrans);

    CDTPPacket packet = gson.fromJson(json, CDTPPacket.class);

    assertThat(packet).isEqualToComparingFieldByField(packetTrans);
  }
}
