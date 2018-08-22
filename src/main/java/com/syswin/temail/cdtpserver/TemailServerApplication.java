package com.syswin.temail.cdtpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.syswin.temail.cdtpserver.properties.TemailServerProperties;


/**
 * @author 姚华成
 * @date 2018/8/7
 */
@EnableConfigurationProperties({TemailServerProperties.class})
@SpringBootApplication
public class TemailServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(TemailServerApplication.class, args);
  }
}
