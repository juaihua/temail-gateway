package com.syswin.temail.gateway.service;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.INTERNAL_ERROR;

import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPServerError;
import com.syswin.temail.ps.server.service.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
public class RequestServiceImpl implements RequestService {

  private final DispatchService dispatchService;
  private final TemailGatewayProperties properties;

  public RequestServiceImpl(WebClient dispatcherWebClient,
      TemailGatewayProperties properties) {
    dispatchService = new DispatchService(dispatcherWebClient);
    this.properties = properties;
  }

  @Override
  public void handleRequest(CDTPPacket reqPacket, CDTPPacket respPacket) {
    dispatchService.dispatch(properties.getDispatchUrl(), new CDTPPacketTrans(reqPacket),
        clientResponse -> clientResponse
            .bodyToMono(String.class)
            .subscribe(response -> {
              if (response != null) {
                // 后台正常返回
                respPacket.setData(response.getBytes());
              } else {
                errorPacket(respPacket, INTERNAL_ERROR.getCode(), "dispatcher请求没有从服务器端返回结果对象：");
              }
              // 请求的数据可能加密，而返回的数据没有加密，需要设置加密标识
              respPacket.getHeader().setDataEncryptionMethod(0);
            }), t -> {
          log.error("调用dispatcher请求出错", t);
          errorPacket(respPacket, INTERNAL_ERROR.getCode(), t.getMessage());
        });
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
