package com.syswin.temail.gateway.service;

public class AuthServiceHttpClientAsncConsumerTest extends AbstractAuthServiceConsumerTest {

  protected AuthService getAuthService(String url) {
    return new AuthServiceHttpClientAsync(url);
  }
}