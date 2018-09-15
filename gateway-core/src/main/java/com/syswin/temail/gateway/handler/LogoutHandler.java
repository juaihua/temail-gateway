package com.syswin.temail.gateway.handler;

import com.syswin.temail.gateway.entity.CDTPPacket;

public interface LogoutHandler {

  void onSucceeded(CDTPPacket request, CDTPPacket response);
}
