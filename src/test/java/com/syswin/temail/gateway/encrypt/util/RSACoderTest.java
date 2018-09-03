package com.syswin.temail.gateway.encrypt.util;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class RSACoderTest {

  private static RSACoder rsaCoder;

  private static String testData;

  private static byte[] testDataBytes;

  private static Charset charset;

  private static Base64Coder base64Coder;

  private static byte[] privateKey;

  private static byte[] publicKey;

  @BeforeClass
  public static void initKeys() {
    try {
      rsaCoder = new RSACoder();
      charset = Charset.forName("utf-8");
      testData = "this is testing Data";
      log.info("RSA测试数据：{}", testData);
      testDataBytes = testData.getBytes(charset);
      base64Coder = new Base64Coder();

      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSACoder.KEY_ALGORITHM);
      keyPairGenerator.initialize(2048);
      KeyPair keyPair = keyPairGenerator.generateKeyPair();
      log.info("生成公钥： {}", keyPair.getPublic().toString());
      log.info("生成私钥： {}", keyPair.getPublic().toString());
      publicKey = keyPair.getPublic().getEncoded();
      privateKey = keyPair.getPrivate().getEncoded();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
  }


  @Test
  public void signAndValid()
      throws InvalidKeySpecException, NoSuchAlgorithmException,
      InvalidKeyException, SignatureException {
    byte[] encryptData = rsaCoder.signData(privateKey, testDataBytes, "MD5withRSA");
    log.info("私钥签名后数据base64： {}", base64Coder.encrypt(encryptData));
    boolean result = rsaCoder.validSign(publicKey, testDataBytes, encryptData, "MD5withRSA");
    log.info("公钥验签结果：{} ", result);
    assertThat(result).isTrue();
  }


  @Test
  public void encryptByPrivateAndDecryptByPublic()
      throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException,
      BadPaddingException, InvalidKeyException, InvalidKeySpecException {
    byte[] encryptData = rsaCoder.encryptByPrivate(privateKey, testDataBytes);
    log.info("私钥加密后的数据base64： {}", base64Coder.encrypt(encryptData));
    byte[] decryptData = rsaCoder.decryptByPublic(publicKey, encryptData);
    log.info("公钥解密后的数据： {}", new String(decryptData, charset));
    assertThat(new String(decryptData, charset)).isEqualTo(testData);
  }


  @Test
  public void encryptByPublicAndDecryptByPrivate()
      throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException,
      BadPaddingException, InvalidKeyException, InvalidKeySpecException {
    byte[] encryptData = rsaCoder.encryptByPublic(publicKey, testDataBytes);
    log.info("公钥加密后的数据base64： {}", base64Coder.encrypt(encryptData));
    byte[] decryptData = rsaCoder.decryptByPrivate(privateKey, encryptData);
    log.info("私钥解密后的数据： {}", new String(decryptData, charset));
    assertThat(new String(decryptData, charset)).isEqualTo(testData);
  }

}
