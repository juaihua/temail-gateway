package com.syswin.temail.gateway.service;

import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.function.Consumer;

/**
 * @author 姚华成
 * @date 2018-11-02
 */
public interface AuthService {

  void validSignature(CDTPPacket reqPacket, Consumer<Response> successConsumer,
      Consumer<Response> failedConsumer);
}
