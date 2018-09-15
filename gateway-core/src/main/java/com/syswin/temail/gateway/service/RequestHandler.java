package com.syswin.temail.gateway.service;

import com.syswin.temail.gateway.entity.CDTPPacket;
import java.util.function.Consumer;

public interface RequestHandler {

  void handleRequest(CDTPPacket packet, Consumer<CDTPPacket> responseHandler);
}
