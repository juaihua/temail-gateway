package com.syswin.temail.gateway.service;

public class AuthServiceWebClientConsumerTest extends AbstractAuthServiceConsumerTest {

  protected AuthService getAuthService(String url) {
    return new AuthServiceWebClient(url);
  }
}