package com.syswin.temail.gateway.service;

import com.syswin.temail.gateway.entity.Response;

public interface DispatchCallback {

  void onSuccess(Response response);

  void onError(Throwable throwable);
}
