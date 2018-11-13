package com.syswin.temail.gateway.service;

import com.syswin.temail.gateway.entity.Response;
import java.util.function.Consumer;

public interface AuthService {

  void validSignature(byte[] payload, Consumer<Response> successConsumer,
      Consumer<Response> failedConsumer);
}
