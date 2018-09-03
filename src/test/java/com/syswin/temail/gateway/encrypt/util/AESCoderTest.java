package com.syswin.temail.gateway.encrypt.util;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class AESCoderTest {

  private Base64Coder base64Coder;

  private AESCoder aesCoder;

  private Charset charset;

  private String testData;

  private byte[] key;

  private byte[] testDataBytes;

  @Before
  public void init() throws Exception {
    base64Coder = new Base64Coder();
    aesCoder = new AESCoder();
    charset = Charset.forName("utf-8");
    testData = "i am testing data, 我是测试数据！";
    testDataBytes = testData.getBytes(charset);
    log.info("AES测试数据： {}", testData);
    key = geneKey("AZaz09");
  }

  /**
   * @param seed base64 str
   * @return
   */
  public byte[] geneKey(String seed) throws Exception {
    SecureRandom secureRandom = null;
    if (seed != null) {
      secureRandom = new SecureRandom(base64Coder.decrypt(seed));
    } else {
      secureRandom = new SecureRandom();
    }

    KeyGenerator keyGenerator = KeyGenerator.getInstance(AESCoder.ALGORITHM);
    keyGenerator.init(secureRandom);
    SecretKey secretKey = keyGenerator.generateKey();
    byte[] key = secretKey.getEncoded();
    log.info("生成AES秘钥base64为： {}", base64Coder.encrypt(key));
    return key;
  }


  @Test
  public void testEncryptAndDecrypt()
      throws NoSuchAlgorithmException, BadPaddingException,
      NoSuchPaddingException, IllegalBlockSizeException,
      InvalidKeyException {
    byte[] encryData = aesCoder.encrypt(key, testDataBytes);
    log.info("加密后的数据base64: {}", base64Coder.encrypt(encryData));
    byte[] decryptData = aesCoder.decrypt(key, encryData);
    String decryptDataStr = new String(decryptData, charset);
    log.info("解密后的数据：{}", decryptDataStr);
    assertThat(testData).isEqualTo(decryptDataStr);
  }

}
