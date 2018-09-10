package com.syswin.temail.gateway.service;

import static java.util.Collections.singletonList;

import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.TemailGatewayProperties.Instance;
import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.gateway.entity.Session;
import com.syswin.temail.gateway.entity.TemailAccoutLocation;
import com.syswin.temail.gateway.entity.TemailAccoutLocations;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
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

  private final TemailGatewayProperties properties;

  private final WebClient statusWebClient;

  // a async queue used for retry failed task
  private final PendingTaskQueue<Pair> pendingTaskQueue = new PendingTaskQueue<>(
      5000,
      pair -> reqUpdSts4Upd(pair.getTemailAccoutLocations(), pair.getTemailAcctUptOptType(), ignored -> {})
  );

  private final ParameterizedTypeReference<Response<Void>> typeReference = new ParameterizedTypeReference<Response<Void>>() {
  };

  @Autowired
  public RemoteStatusService(TemailGatewayProperties properties, WebClient statusWebClient) {
    this.properties = properties;
    this.statusWebClient = statusWebClient;
    this.pendingTaskQueue.run();
  }

  public void addSession(String temail, String deviceId, Consumer<Response<Void>> consumer) {
    updSessionByType(temail, deviceId, TemailAcctUptOptType.add, consumer);
  }

  public void removeSession(String temail, String deviceId, Consumer<Response<Void>> consumer) {
    updSessionByType(temail, deviceId, TemailAcctUptOptType.del, consumer);
  }

  private void updSessionByType(String temail, String deviceId, TemailAcctUptOptType optType, Consumer<Response<Void>> consumer) {
    reqUpdSts4Upd(
        new TemailAccoutLocations(singletonList(buildAcctSts(temail, deviceId))),
        optType,
        consumer);
  }

  void removeSessions(Collection<Session> sessions, Consumer<Response<Void>> consumer) {
    List<TemailAccoutLocation> statuses = new ArrayList<>(sessions.size());
    for (Session session : sessions) {
      statuses.add(buildAcctSts(session.getTemail(), session.getDeviceId()));
    }

    reqUpdSts4Upd(new TemailAccoutLocations(statuses), TemailAcctUptOptType.del, consumer);
  }

  private TemailAccoutLocation buildAcctSts(String temail, String deviceId) {
    Instance instance = properties.getInstance();
    return new TemailAccoutLocation(temail, deviceId,
        instance.getHostOf(), instance.getProcessId(),
        properties.getRocketmq().getMqTopic(), instance.getMqTag());
  }

  private void reqUpdSts4Upd(TemailAccoutLocations temailAccoutLocations, TemailAcctUptOptType type, Consumer<Response<Void>> consumer) {
    statusWebClient.method(type.getMethod())
        .uri(properties.getUpdateSocketStatusUrl())
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .syncBody(temailAccoutLocations)
        .exchange()
        .subscribe(clientResponse -> {
          if (!clientResponse.statusCode().is2xxSuccessful()) {
            log.debug("upd temailAccoutLocations fail {} , will try agagin later! ", clientResponse.statusCode());
            pendingTaskQueue.addTask(new Pair(type, temailAccoutLocations));
          } else {
            clientResponse.bodyToMono(typeReference)
                .subscribe(result -> {
              log.debug("response from status server: {}", result.toString());
              consumer.accept(result);
            });
          }
        });
  }

  enum TemailAcctUptOptType {
    add(HttpMethod.POST),
    del(HttpMethod.PUT);

    private HttpMethod method;

    TemailAcctUptOptType(HttpMethod method) {
      this.method = method;
    }

    public HttpMethod getMethod() {
      return method;
    }
  }
}
