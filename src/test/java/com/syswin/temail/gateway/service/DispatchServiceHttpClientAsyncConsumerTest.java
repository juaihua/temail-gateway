package com.syswin.temail.gateway.service;

public class DispatchServiceHttpClientAsyncConsumerTest extends AbstractDispatchServiceConsumerTest {

  protected DispatchService getDispatchService(String url) {
    return new DispatchServiceHttpClientAsync(url);
  }
}
