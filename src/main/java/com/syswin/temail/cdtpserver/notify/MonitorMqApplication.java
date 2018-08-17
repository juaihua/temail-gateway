package com.syswin.temail.cdtpserver.notify;

import java.lang.invoke.MethodHandles;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.syswin.temail.cdtpserver.entity.TemailMqInfo;
import com.syswin.temail.cdtpserver.properties.TemailServerProperties;
import com.syswin.temail.cdtpserver.utils.TemailMqInfBuilder;

@Component
@Order(2)
public class MonitorMqApplication implements ApplicationRunner {
  private static final Logger LOGGER = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass());

  @Resource
  private TemailServerProperties properties;
  
  @Override
  public void run(ApplicationArguments args) throws Exception {
    TemailMqInfo  temailMqInfo  = TemailMqInfBuilder.getTemailMqInf();
     LOGGER.info("启动线程监听MQ:{}", temailMqInfo.getMqTopic());
     MonitorMqRunnable  monitorMqRunnable =new   MonitorMqRunnable();
     monitorMqRunnable.setTemailServerConfig(properties);
     monitorMqRunnable.setTemailMqInfo(temailMqInfo);
     Thread monitorThread = new Thread(monitorMqRunnable, "monitorThread") ;
     monitorThread.start();
  }
  
  
 

}
