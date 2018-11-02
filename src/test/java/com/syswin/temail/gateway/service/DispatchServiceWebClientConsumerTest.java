package com.syswin.temail.gateway.service;

public class DispatchServiceWebClientConsumerTest extends AbstractDispatchServiceConsumerTest {

  @Override
  protected DispatchService getDispatchService(String url) {
    return new DispatchServiceWebClient(url);
  }
}
