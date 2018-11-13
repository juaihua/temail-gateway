package com.syswin.temail.gateway.notify;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.io.IOException;

public class PacketDeserializer extends TypeAdapter<CDTPPacket> {

  private final Gson gson = new Gson();
  private final TypeAdapter<CDTPPacket> packetAdapter = gson.getAdapter(CDTPPacket.class);
  private final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);

  @Override
  public void write(JsonWriter out, CDTPPacket value) throws IOException {
    packetAdapter.write(out, value);
  }

  @Override
  public CDTPPacket read(JsonReader in) throws IOException {
    JsonElement tree = elementAdapter.read(in);
    JsonElement dataElement = tree.getAsJsonObject().get("data");
    tree.getAsJsonObject().remove("data");

    CDTPPacket packet = gson.fromJson(tree, CDTPPacket.class);
    if (dataElement != null) {
      packet.setData(dataElement.getAsString().getBytes());
    }

    return packet;
  }
}
