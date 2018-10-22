package com.syswin.temail.gateway.service;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.INTERNAL_ERROR;
import static com.syswin.temail.ps.common.utils.SignatureUtil.resetSignature;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPServerError;
import com.syswin.temail.ps.server.service.RequestService;
import java.io.IOException;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

@Slf4j
public class RequestServiceHttpClientAsync implements RequestService {

  private final DispatchServiceHttpClientAsync dispatchService;

  public RequestServiceHttpClientAsync(String dispatchUrl) {
    this.dispatchService = new DispatchServiceHttpClientAsync(dispatchUrl);
  }

  @Override
  public void handleRequest(CDTPPacket reqPacket, Consumer<CDTPPacket> responseHandler) {
    Consumer<Throwable> errorHandler = t -> {
      log.error("调用dispatcher请求出错", t);
      errorPacket(reqPacket, INTERNAL_ERROR.getCode(), t.getMessage());
      resetSignature(reqPacket);
      responseHandler.accept(reqPacket);
    };

    dispatchService.dispatch(new CDTPPacketTrans(reqPacket),
        httpResponse -> {
          try {
            HttpEntity entity = httpResponse.getEntity();
            byte[] responseData = EntityUtils.toByteArray(entity);
            reqPacket.setData(responseData);
            resetSignature(reqPacket);
            responseHandler.accept(reqPacket);
          } catch (IOException e) {
            errorHandler.accept(e);
          }
        },
        errorHandler);
  }

  private void errorPacket(CDTPPacket packet, int code, String message) {
    packet.setCommandSpace(CHANNEL_CODE);
    packet.setCommand(INTERNAL_ERROR.getCode());

    CDTPServerError.Builder builder = CDTPServerError.newBuilder();
    builder.setCode(code);
    builder.setDesc(message);
    packet.setData(builder.build().toByteArray());
  }
}
