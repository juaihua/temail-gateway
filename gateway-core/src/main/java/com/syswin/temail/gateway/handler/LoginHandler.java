package com.syswin.temail.gateway.handler;

import com.syswin.temail.gateway.entity.CDTPPacket;

public interface LoginHandler {

  void onSucceed(CDTPPacket request, CDTPPacket response);

  void onFailed(CDTPPacket response);
}
