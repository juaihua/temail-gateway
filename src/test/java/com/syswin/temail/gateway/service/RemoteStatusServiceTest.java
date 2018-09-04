package com.syswin.temail.gateway.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.gateway.entity.Session;
import com.syswin.temail.gateway.entity.TemailAccoutLocation;
import com.syswin.temail.gateway.entity.TemailAccoutLocations;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.Arrays.asList;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.waitAtMost;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class RemoteStatusServiceTest {

  private static final Gson GSON = new Gson();

  private final TemailGatewayProperties properties = new TemailGatewayProperties();
  private final RemoteStatusService remoteStatusService = new RemoteStatusService(properties, WebClient.create());

  private final List<Response<Void>> results = new ArrayList<>();

  @ClassRule
  public static final WireMockRule WIRE_MOCK_RULE = new WireMockRule(wireMockConfig().dynamicPort());

  @BeforeClass
  public static void initMockServer() {
    stubFor(post(urlEqualTo("/locations"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .withStatus(SC_CREATED)
            .withBody(GSON.toJson(Response.ok()))));

    stubFor(put(urlEqualTo("/locations"))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withStatus(SC_OK)
                .withBody(GSON.toJson(Response.ok()))));

    List<TemailAccoutLocation> stses = asList(
        new TemailAccoutLocation("sean@temail.com", "123456", "192.168.197.23", "232", "topic", "mqTopic"),
        new TemailAccoutLocation("jean@temail.com", "654321", "192.168.197.24", "235", "topid", "mqTopid"));

    TemailAccoutLocations data = new TemailAccoutLocations(stses);
    stubFor(get(urlMatching("/locations/.*"))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withStatus(SC_OK)
                .withBody(GSON.toJson(Response.ok(data)))));
  }

  @Before
  public void init() {
    properties.setUpdateSocketStatusUrl("http://localhost:" + WIRE_MOCK_RULE.port() + "/locations");
    results.clear();
  }


  @Test
  public void testAddSession() {
    remoteStatusService.addSession("sean_1@temail.com", "12345678", results::add);
    waitAtMost(1, TimeUnit.SECONDS).until(() -> !results.isEmpty());
    assertThat(results.get(0)).isInstanceOf(Response.class);
  }

  @Test
  public void testRemoveSession() {
    remoteStatusService.removeSession("sean_1@temail.com", "12345678", results::add);
    waitAtMost(1, TimeUnit.SECONDS).until(() -> !results.isEmpty());
    assertThat(results.get(0)).isInstanceOf(Response.class);
  }

  @Test
  public void testRemoveSessions() {
    List<Session> sessions = asList(
      new Session("sean@temail.com", "123"),
      new Session("jean@temail.com", "123"));

    remoteStatusService.removeSessions(sessions, results::add);
    waitAtMost(1, TimeUnit.SECONDS).until(() -> !results.isEmpty());
    assertThat(results.get(0)).isInstanceOf(Response.class);
  }
}
