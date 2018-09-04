package com.syswin.temail.gateway.encrypt.util;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECFieldF2m;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import sun.security.ec.ECPrivateKeyImpl;
import sun.security.ec.ECPublicKeyImpl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class ECCCoderTest {

  private static Base64Coder base64Coder;

  private static ECCCoder eccCoder;

  private static String testData;

  private static Charset charset;

  private static byte[] dataBytes;

  private static byte[] publicKeyBytes;

  private static byte[] privateKeyBytes;

  @BeforeClass
  public static void init() throws InvalidKeyException {
    base64Coder = new Base64Coder();
    charset = Charset.forName("utf-8");
    testData = "i am testing data, 我是测试数据！";
    dataBytes = testData.getBytes(charset);
    eccCoder = new ECCCoder();
    initKeys();
  }

  public static void initKeys() throws InvalidKeyException {
    BigInteger x1 = new BigInteger("2fe13c0537bbc11acaa07d793de4e6d5e5c94eee8", 16);
    BigInteger x2 = new BigInteger("289070fb05d38ff58321f2e800536d538ccdaa3d9", 16);
    ECPoint g = new ECPoint(x1, x2);

    BigInteger n = new BigInteger("5846006549323611672814741753598448348329118574063", 10);
    int h = 2;
    int m = 163;
    int[] ks = {7, 6, 3};
    ECFieldF2m ecField = new ECFieldF2m(m, ks);
    BigInteger a = new BigInteger("1", 2);
    BigInteger b = new BigInteger("1", 2);
    EllipticCurve ellipticCurve = new EllipticCurve(ecField, a, b);
    ECParameterSpec ecParameterSpec = new ECParameterSpec(ellipticCurve, g, n, h);

    ECPublicKey publicKey = new ECPublicKeyImpl(g, ecParameterSpec);
    publicKeyBytes = publicKey.getEncoded();
    log.info("gene ECC public key: {}", publicKey.toString());

    BigInteger s = new BigInteger("1234006549323611672814741753598448348329118574063", 10);
    ECPrivateKey privateKey = new ECPrivateKeyImpl(s, ecParameterSpec);
    privateKeyBytes = privateKey.getEncoded();

  }

  @Test
  public void encryptByPublicAndDecryptByPrivate()
      throws IllegalBlockSizeException, BadPaddingException,
      InvalidAlgorithmParameterException, InvalidKeyException {
    byte[] encryptData = eccCoder.encryptByPublic(publicKeyBytes, dataBytes);
    log.info("ECC公钥加密数据base64后： {}", base64Coder.encrypt(encryptData));
    byte[] decryptData = eccCoder.decryptByPrivate(privateKeyBytes, encryptData);
    String decryptDataStr = new String(decryptData, charset);
    log.info("ECC私钥解密后数据为： {}", decryptDataStr);
    assertThat(decryptDataStr).isEqualTo(testData);
  }

  @Test
  public void encryptByPrivateAndDecryptByPublic()
      throws IllegalBlockSizeException, BadPaddingException,
      InvalidAlgorithmParameterException, InvalidKeyException {
    byte[] encryptData = eccCoder.encryptByPrivate(privateKeyBytes, dataBytes);
    log.info("ECC私钥加密数据base64后： {}", base64Coder.encrypt(encryptData));
    byte[] decryptData = eccCoder.decryptByPublic(publicKeyBytes, encryptData);
    String decryptDataStr = new String(decryptData, charset);
    log.info("ECC公钥钥解密后数据为： {}", decryptDataStr);
    assertThat(decryptDataStr).isEqualTo(testData);
  }

}
