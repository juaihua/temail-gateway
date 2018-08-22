package com.syswin.temail.cdtpserver.handler;

import java.io.IOException;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

public class SilentResponseErrorHandler extends DefaultResponseErrorHandler {


  @Override
  public void handleError(ClientHttpResponse clientHttpResponse) {}
}
