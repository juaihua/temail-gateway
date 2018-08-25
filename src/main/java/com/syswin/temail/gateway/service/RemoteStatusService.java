package com.syswin.temail.gateway.service;

import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.entity.Session;
import com.syswin.temail.gateway.entity.TemailSocketInfo;
import com.syswin.temail.gateway.entity.TemailSocketInstance;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * @author 姚华成
 * @date 2018-8-24
 */
@Service
public class RemoteStatusService {

  private final TemailGatewayProperties properties;
  private final WebClient statusWebClient;

  @Autowired
  public RemoteStatusService(TemailGatewayProperties properties, WebClient statusWebClient) {
    this.properties = properties;
    this.statusWebClient = statusWebClient;
  }

  public Mono<ClientResponse> addSession(String temail, String deviceId) {
    return updateRemoteStatus(temail, deviceId, "add");
  }

  public void removeSession(String temail, String deviceId) {
    updateRemoteStatus(temail, deviceId, "del");
  }

  public void removeSessions(List<Session> sessions) {
    if (sessions != null) {
      // 让状态服务器提供指更新的能力
      for (Session session : sessions) {
        removeSession(session.getTemail(), session.getDeviceId());
      }
    }
  }

  private Mono<ClientResponse> updateRemoteStatus(String temail, String deviceId, String opType) {
    TemailSocketInfo temailChannel = new TemailSocketInfo(temail, opType,
        new TemailSocketInstance(deviceId,
            properties.getHostOf(),
            properties.getProcessId(),
            properties.getMqTopic(),
            properties.getMqTag()));
    // 同步远程状态
    return statusWebClient.post()
        .uri(properties.getUpdateSocketStatusUrl())
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .syncBody(temailChannel)
        .exchange()
        .doOnSuccess(clientResponse -> {
          // TODO(姚华成): 如果返回结果码不正确，则保存到重试队列里
        })
        .doOnError(throwable -> {
          // TODO(姚华成): 如果返回结果码不正确，则保存到重试队列里
        });

  }

}
