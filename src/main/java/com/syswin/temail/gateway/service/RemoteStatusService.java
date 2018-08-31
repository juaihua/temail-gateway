package com.syswin.temail.gateway.service;

import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.TemailGatewayProperties.Instance;
import com.syswin.temail.gateway.entity.ComnRespData;
import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.gateway.entity.Session;
import com.syswin.temail.gateway.entity.TemailAccoutLocation;
import com.syswin.temail.gateway.entity.TemailAccoutLocations;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class RemoteStatusService {

  @Resource
  private final TemailGatewayProperties properties;

  @Resource
  private final WebClient statusWebClient;

  // a async queue used for retry failed task
  private final PendingTaskQueue<Pair> pendingTaskQueue = new PendingTaskQueue<Pair>(
      5000,
      pair -> reqUpdSts4Upd(pair.getTemailAccoutLocations(), pair.getTemailAcctUptOptType(), null)
  );

  @Autowired
  public RemoteStatusService(TemailGatewayProperties properties, WebClient statusWebClient) {
    this.properties = properties;
    this.statusWebClient = statusWebClient;
    this.pendingTaskQueue.run();
  }

  public void addSession(String temail, String deviceId, Consumer consumer) {
    updSessionByType(temail, deviceId, TemailAcctUptOptType.add, consumer);
  }

  public void removeSession(String temail, String deviceId, Consumer consumer) {
    updSessionByType(temail, deviceId, TemailAcctUptOptType.del, consumer);
  }

  public void updSessionByType(String temail, String deviceId, TemailAcctUptOptType optType,Consumer consumer) {
    reqUpdSts4Upd(new TemailAccoutLocations(
        new ArrayList<TemailAccoutLocation>() {{
          add(buildAcctSts(temail, deviceId));
        }}), optType, consumer);
  }

  public void removeSessions(Iterable<Session> sessions,Consumer consumer) {
    if(sessions == null) return;
    reqUpdSts4Upd(new TemailAccoutLocations(new ArrayList<TemailAccoutLocation>() {{
      for (Session session : sessions) {
        add(buildAcctSts(session.getTemail(), session.getDeviceId()));
      }
    }}), TemailAcctUptOptType.del,consumer);
  }

  private TemailAccoutLocation buildAcctSts(String temail, String deviceId) {
    Instance instance = properties.getInstance();
    return new TemailAccoutLocation(temail, deviceId,
        instance.getHostOf(), instance.getProcessId(),
        properties.getRocketmq().getMqTopic(), instance.getMqTag());
  }

  public TemailAccoutLocations locateTemailAcctSts(String temail) {
    Response<TemailAccoutLocations> res =
        statusWebClient.get().uri(properties.getUpdateSocketStatusUrl()+"/{temail}",temail)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .retrieve().bodyToMono(new ParameterizedTypeReference<Response<TemailAccoutLocations>>(){}).block();
    return res.getData();
  }

  private void reqUpdSts4Upd(TemailAccoutLocations temailAccoutLocations, TemailAcctUptOptType type, Consumer consumer) {
    statusWebClient.method(type.getMethod())
        .uri(properties.getUpdateSocketStatusUrl())
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .syncBody(temailAccoutLocations)
        .exchange()
        .subscribe(clientResponse -> {
          if (!clientResponse.statusCode().is2xxSuccessful()) {
            log.info("upd temailAccoutLocations fail {} , will try agagin later! ", clientResponse.statusCode());
            pendingTaskQueue.addTask(new Pair(type, temailAccoutLocations));
          } else {
            clientResponse.bodyToMono(new ParameterizedTypeReference<Response<ComnRespData>>() {
            }).subscribe(result -> {
              log.debug("response from status server: {}", result.toString());
              Optional.ofNullable(consumer).ifPresent(consumer1 -> consumer1.accept(result));
            });
          }
        });
  }

  static enum TemailAcctUptOptType {
    add(HttpMethod.POST),
    del(HttpMethod.DELETE);

    private HttpMethod method;

    private TemailAcctUptOptType(HttpMethod method) {
      this.method = method;
    }

    public HttpMethod getMethod() {
      return method;
    }
  }
}
