package com.syswin.temail.gateway.service;

import com.syswin.temail.gateway.entity.CDTPPacket;
import com.syswin.temail.gateway.entity.Response;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ClientResponse;

public interface DispatchCallback {

   public  Response   onsuccess(Response response);

   public  void    onError(int  errorCode,  String  errorMsg);
}
