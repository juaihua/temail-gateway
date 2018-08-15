package com.syswin.temail.cdtpserver.notify;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class MonitorMqApplication implements ApplicationRunner {
  private static final Logger LOGGER = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass());
  @Override
  public void run(ApplicationArguments args) throws Exception {
    LOGGER.info("启动线程监听MQ");
     MonitorMqRunnable  monitorMqRunnable =new   MonitorMqRunnable();
     Thread monitorThread = new Thread(monitorMqRunnable, "monitorThread") ;
     monitorThread.start();

  }

}
