package com.syswin.temail.gateway.service;

import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.codec.CommandAwarePacketUtil;

public class AuthServiceWebClientConsumerTest extends AbstractAuthServiceConsumerTest {

  private CommandAwarePacketUtil packetUtil = new CommandAwarePacketUtil(new TemailGatewayProperties());

  protected AuthService getAuthService(String url) {
    return new AuthServiceWebClient(url, packetUtil);
  }
}