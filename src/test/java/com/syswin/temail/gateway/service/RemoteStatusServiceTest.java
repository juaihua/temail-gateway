package com.syswin.temail.gateway.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.syswin.temail.gateway.TemailGatewayApplication;
import com.syswin.temail.gateway.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TemailGatewayApplication.class,
    properties = {"temail.gateway.updateSocketStatusUrl=http://localhost:9100/locations"})
@ActiveProfiles({"debug", "dev"})
public class RemoteStatusServiceTest {

  private static final Gson GSON = new Gson();

  @Autowired
  private RemoteStatusService remoteStatusService;

  private List<Object> results = new ArrayList();

  @ClassRule
  public static final WireMockRule WIRE_MOCK_RULE = new WireMockRule(9100);

  @BeforeClass
  public static void initMockServer() {
    stubFor(post(urlEqualTo("/locations"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .withStatus(SC_CREATED)
            .withBody(GSON.toJson(Response.ok(new ComnRespData(true))))));

    stubFor(delete(urlEqualTo("/locations"))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withStatus(SC_OK)
                .withBody(GSON.toJson(Response.ok(new ComnRespData(true))))));

    List<TemailAcctSts> stses = new ArrayList<TemailAcctSts>() {{
      add(new TemailAcctSts("sean@temail.com", "123456", "192.168.197.23", "232", "topic", "mqTopic"));
      add(new TemailAcctSts("jean@temail.com", "654321", "192.168.197.24", "235", "topid", "mqTopid"));
    }};
    TemailAcctStses data = new TemailAcctStses(stses);
    stubFor(get(urlMatching("/locations/.*"))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withStatus(SC_OK)
                .withBody(GSON.toJson(Response.ok(data)))));
  }

  @Before
  public void init() {
    results.clear();
  }


  @Test
  public void testAddSession() {
    remoteStatusService.addSession("sean_1@temail.com", "12345678", p -> results.add(p));
    Awaitility.waitAtMost(1000, TimeUnit.SECONDS).until(() -> !results.isEmpty());
    assertThat(results.get(0)).isInstanceOf(Response.class);
  }

  @Test
  public void testLocateTemailAcctSts() {
    TemailAcctStses response = remoteStatusService.locateTemailAcctSts("sean_1@temail.com");
    Awaitility.waitAtMost(1000, TimeUnit.SECONDS).until(() -> response.getStatuses().size() > 0);
  }

  @Test
  public void testRemoveSession() {
    remoteStatusService.removeSession("sean_1@temail.com", "12345678", p -> results.add(p));
    Awaitility.waitAtMost(1000, TimeUnit.SECONDS).until(() -> !results.isEmpty());
    assertThat(results.get(0)).isInstanceOf(Response.class);
  }

  @Test
  public void testRemoveSessions() {
    List<Session> sessions = new ArrayList<Session>() {{
      add(new Session("sean@temail.com", "123"));
      add(new Session("jean@temail.com", "123"));
    }};
    remoteStatusService.removeSessions(sessions, p -> results.add(p));
    Awaitility.waitAtMost(1000, TimeUnit.SECONDS).until(() -> !results.isEmpty());
    assertThat(results.get(0)).isInstanceOf(Response.class);
  }

  @After
  public void last4Awhile() {
    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}