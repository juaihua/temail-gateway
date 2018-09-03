package com.syswin.temail.gateway.notify;

import static com.syswin.temail.gateway.client.PacketMaker.mqMsgPayload;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.MessagePactProviderRule;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.model.v3.messaging.MessagePact;
import com.google.gson.Gson;
import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.CDTPPacketTrans;
import com.syswin.temail.gateway.service.ChannelHolder;
import io.netty.channel.Channel;
import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;


public class MessageHandlerConsumerTest {

  @Rule
  public final MessagePactProviderRule mockProvider = new MessagePactProviderRule(this);

  private final Channel channel = Mockito.mock(Channel.class);
  private final ChannelHolder channelHolder = Mockito.mock(ChannelHolder.class);

  private final String recipient = "sean@t.email";
  private final Gson gson = new Gson();
  private final CDTPPacketTrans payload = mqMsgPayload(recipient, "bonjour");
  private byte[] currentMessage;

  @Pact(provider = "temail-dispatcher-mq", consumer = "temail-gateway-mq")
  public MessagePact createPact(MessagePactBuilder builder) {
    PactDslJsonBody header = packetJson(payload, bodyJson(payload));

    Map<String, String> metadata = new HashMap<>();
    metadata.put("contentType", "application/json");

    return builder.given("Notification service is available")
        .expectsToReceive("online notification")
        .withMetadata(metadata)
        .withContent(header)
        .toPact();
  }

  @Test
  @PactVerification({"Able to process online notification message"})
  public void test() {
    when(channelHolder.getChannels(recipient)).thenReturn(singletonList(channel));

    MessageHandler messageHandler = new MessageHandler(channelHolder);

    String msg = new String(currentMessage);
    messageHandler.onMessageReceived(msg);

    verify(channel).writeAndFlush(argThat(matchesPayload(payload)));
  }

  public void setMessage(byte[] messageContents) {
    currentMessage = messageContents;
  }

  private ArgumentMatcher<CDTPPacket> matchesPayload(CDTPPacketTrans payload) {
    return packet -> gson.toJson(payload)
        .equals(gson.toJson(new CDTPPacketTrans(packet)));
  }

  private PactDslJsonBody packetJson(CDTPPacketTrans cdtpPacketTrans, PactDslJsonBody body) {
    PactDslJsonBody header = new PactDslJsonBody("header", "", body);

    header.stringValue("deviceId", cdtpPacketTrans.getHeader().getDeviceId());
    header.numberValue("signatureAlgorithm", cdtpPacketTrans.getHeader().getSignatureAlgorithm());
    header.stringValue("signature", cdtpPacketTrans.getHeader().getSignature());
    header.numberValue("dataEncryptionMethod", cdtpPacketTrans.getHeader().getDataEncryptionMethod());
    header.numberValue("timestamp", cdtpPacketTrans.getHeader().getTimestamp());
    header.stringValue("packetId", cdtpPacketTrans.getHeader().getPacketId());
    header.stringValue("sender", cdtpPacketTrans.getHeader().getSender());
    header.stringValue("senderPK", cdtpPacketTrans.getHeader().getSenderPK());
    header.stringValue("receiver", cdtpPacketTrans.getHeader().getReceiver());
    header.stringValue("receiverPK", cdtpPacketTrans.getHeader().getReceiverPK());
    header.stringValue("at", cdtpPacketTrans.getHeader().getAt());
    header.stringValue("topic", cdtpPacketTrans.getHeader().getTopic());
    header.stringValue("extraData", cdtpPacketTrans.getHeader().getExtraData());
    return header;
  }

  private PactDslJsonBody bodyJson(CDTPPacketTrans cdtpPacketTrans) {
    PactDslJsonBody body = new PactDslJsonBody();
    body.numberValue("commandSpace", cdtpPacketTrans.getCommandSpace());
    body.numberValue("command", cdtpPacketTrans.getCommand());
    body.numberValue("version", cdtpPacketTrans.getVersion());
    body.stringValue("data", cdtpPacketTrans.getData());
    return body;
  }
}