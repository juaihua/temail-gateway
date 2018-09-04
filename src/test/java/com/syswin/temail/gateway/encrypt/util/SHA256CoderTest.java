package com.syswin.temail.gateway.encrypt.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class SHA256CoderTest {

  private SHA256Coder sha256Coder = new SHA256Coder();

  private Base64Coder base64Coder = new Base64Coder();

  private String charSet  = "utf-8";

  @Test
  public void testSHA256() throws Exception {
    for(int i = 0; i < 100; i++){
      String testData = "Thank you!"+i;
      log.info("测试数据：{}", testData);

      String encryptedData = sha256Coder
          .encryptAndSwitch2Base64(testData.getBytes(charSet));
      log.info("SHA256加密后base64： {}", encryptedData);

      assertThat(base64Coder.decrypt(encryptedData))
          .isEqualTo(sha256Coder.encrypt(testData.getBytes(charSet)));
    }
  }
}