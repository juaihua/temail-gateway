package com.syswin.temail.gateway;

import com.syswin.temail.ps.server.PsServer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * @author 姚华成
 * @date 2018-10-22
 */
public class TemailGatewayRunner implements ApplicationRunner {

  private PsServer psServer;

  public TemailGatewayRunner(PsServer psServer) {
    this.psServer = psServer;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    psServer.run();
  }
}
