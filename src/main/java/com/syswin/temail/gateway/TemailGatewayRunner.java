package com.syswin.temail.gateway;

import com.syswin.temail.ps.server.GatewayServer;
import com.syswin.temail.ps.server.Stoppable;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;

public class TemailGatewayRunner implements ApplicationRunner, Ordered {

  private final GatewayServer gatewayServer;

  public TemailGatewayRunner(GatewayServer gatewayServer) {
    this.gatewayServer = gatewayServer;
  }

  @Override
  public void run(ApplicationArguments args) {
    final Stoppable stoppable = gatewayServer.run();
    Runtime.getRuntime().addShutdownHook(new Thread(stoppable::stop));
  }

  @Override
  public int getOrder() {
    return 1;
  }
}
