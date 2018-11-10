package com.syswin.temail.gateway.notify;

import static com.syswin.temail.gateway.client.PacketMaker.mqMsgPayload;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
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
import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.codec.CommandAwarePacketUtil;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import com.syswin.temail.ps.common.packet.SimplePacketUtil;
import com.syswin.temail.ps.server.service.ChannelHolder;
import io.netty.channel.Channel;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;


@Ignore
@Slf4j
public class MessageHandlerConsumerTest {

  @Rule
  public final MessagePactProviderRule mockProvider = new MessagePactProviderRule(this);

  private final Channel channel = Mockito.mock(Channel.class);
  private final ChannelHolder channelHolder = Mockito.mock(ChannelHolder.class);
  private final CommandAwarePacketUtil packetUtil = new CommandAwarePacketUtil(new TemailGatewayProperties());

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

    MessageHandler messageHandler = new MessageHandler(channelHolder, packetUtil);

    String msg = new String(currentMessage);
    messageHandler.onMessageReceived(msg);

    verify(channel).writeAndFlush(argThat(matchesPayload(payload)));
  }

  public void setMessage(byte[] messageContents) {
    currentMessage = messageContents;
  }

  private ArgumentMatcher<CDTPPacket> matchesPayload(CDTPPacketTrans payload) {
    return packet -> {

      CDTPPacketTrans actual = SimplePacketUtil.INSTANCE.toTrans(packet);
      assertThat(actual).isEqualToIgnoringGivenFields(payload, "header");
      assertThat(actual.getHeader()).isEqualToIgnoringGivenFields(payload.getHeader(), "packetId", "signature");
      assertThat(actual.getHeader().getSignature()).isNull();
      assertThat(actual.getHeader().getPacketId()).isNotEmpty();
      return true;
    };
  }

  private PactDslJsonBody packetJson(CDTPPacketTrans cdtpPacketTrans, PactDslJsonBody body) {
    PactDslJsonBody header = new PactDslJsonBody("header", "", body);

    setStringIfNotNull(header, "deviceId", cdtpPacketTrans.getHeader().getDeviceId());
    header.numberValue("signatureAlgorithm", cdtpPacketTrans.getHeader().getSignatureAlgorithm());
    setStringIfNotNull(header, "signature", cdtpPacketTrans.getHeader().getSignature());
    header.numberValue("dataEncryptionMethod", cdtpPacketTrans.getHeader().getDataEncryptionMethod());
    header.numberValue("timestamp", cdtpPacketTrans.getHeader().getTimestamp());
    setStringIfNotNull(header, "packetId", cdtpPacketTrans.getHeader().getPacketId());
    setStringIfNotNull(header, "sender", cdtpPacketTrans.getHeader().getSender());
    setStringIfNotNull(header, "senderPK", cdtpPacketTrans.getHeader().getSenderPK());
    setStringIfNotNull(header, "receiver", cdtpPacketTrans.getHeader().getReceiver());
    setStringIfNotNull(header, "receiverPK", cdtpPacketTrans.getHeader().getReceiverPK());
    setStringIfNotNull(header, "at", cdtpPacketTrans.getHeader().getAt());
    setStringIfNotNull(header, "topic", cdtpPacketTrans.getHeader().getTopic());
    setStringIfNotNull(header, "extraData", cdtpPacketTrans.getHeader().getExtraData());
    return header;
  }

  private void setStringIfNotNull(PactDslJsonBody header, String key, String value) {
    if (value != null) {
      header.stringValue(key, value);
    }
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