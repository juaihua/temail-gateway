package com.syswin.temail.ps.client;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.server.service.RequestService;
import java.util.function.Consumer;

/**
 * @author 姚华成
 * @date 2018-9-20
 */
class TestRequestService implements RequestService {

  private TestRequestHandler handler;

  TestRequestService(TestRequestHandler handler) {
    this.handler = handler;
  }

  @Override
  public void handleRequest(CDTPPacket reqPacket, Consumer<CDTPPacket> responseHandler) {
    CDTPPacket respPacket = handler.dispatch(reqPacket);
    responseHandler.accept(respPacket);
  }
}