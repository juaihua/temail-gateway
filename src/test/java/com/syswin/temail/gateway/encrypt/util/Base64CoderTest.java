package com.syswin.temail.gateway.encrypt.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class Base64CoderTest {

  private String data = "i am testing data";

  private Base64Coder base64Coder;

  @Before
  public void info(){
    log.info("Base64测试数据： {}", data);
    base64Coder = new Base64Coder();
  }

  @Test
  public void encryptAndDecrypt() throws Exception{
      byte[] dataByte = data.getBytes("utf-8");
      String encryData = base64Coder.encrypt(data);
      log.info("base64加密后： {}", encryData);

      byte[] decryData = base64Coder.decrypt(encryData);
      String decryptedData = new String(decryData,"utf-8");
      log.info("base64解密后： {}", new String(decryData,"utf-8"));

      assertThat(data).isEqualTo(decryptedData);
  }
}