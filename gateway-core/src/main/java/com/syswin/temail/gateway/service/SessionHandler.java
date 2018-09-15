package com.syswin.temail.gateway.service;

import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.Session;
import com.syswin.temail.gateway.handler.LoginHandler;
import com.syswin.temail.gateway.handler.LogoutHandler;
import java.util.Collection;

public interface SessionHandler {

  void login(CDTPPacket packet, LoginHandler loginHandler);

  void logout(CDTPPacket packet, LogoutHandler logoutHandler);

  void disconnect(Collection<Session> sessions);
}
