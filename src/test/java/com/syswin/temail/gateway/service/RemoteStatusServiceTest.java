package com.syswin.temail.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"temail.gateway.updateSocketStatusUrl=http://localhost:9100/locations"})
public class RemoteStatusServiceTest {

  @Autowired
  private RemoteStatusService remoteStatusService;

  @Test
  public void testAddSession() {
    // cur junit version is lower than 4.12, can not design the sequence of the method executes
    remoteStatusService.addSession("sean_1@temail.com","12345678");
    remoteStatusService.locateTemailAcctSts("sean_1@temail.com");
    remoteStatusService.removeSession("sean_1@temail.com","12345678");
  }

  //@Test
  public void testLocateTemailAcctSts(){
    remoteStatusService.locateTemailAcctSts("sean_1@temail.com");
  }

  //@Test
  public void testRemoveSession() {
    remoteStatusService.removeSession("sean_1@temail.com","12345678");
  }

  @After
  public void last4Awhile(){
     try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}