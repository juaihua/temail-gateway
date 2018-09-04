package com.syswin.temail.gateway.encrypt.util;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.syswin.temail.gateway.encrypt.util.SM2Coder.a;
import static com.syswin.temail.gateway.encrypt.util.SM2Coder.b;
import static com.syswin.temail.gateway.encrypt.util.SM2Coder.n;
import static com.syswin.temail.gateway.encrypt.util.SM2Coder.p;
import static com.syswin.temail.gateway.encrypt.util.SM2Coder.xg;
import static com.syswin.temail.gateway.encrypt.util.SM2Coder.yg;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class SM2CoderTest {

  private static Base64Coder base64Coder;

  private static SM2Coder sm2Coder;

  private static String testData;

  private static Charset charset;

  private static byte[] dataBytes;

  private static byte[] publicKeyBytes;

  private static byte[] privateKeyBytes;

  private static ECPoint publicKey;

  private static BigInteger privateKey;

  private static ECCurve.Fp curve;

  private static ECPoint G;

  @BeforeClass
  public static void init() throws InvalidKeyException {
    base64Coder = new Base64Coder();
    charset = Charset.forName("utf-8");
    testData = "i am testing data, 我是测试数据！";
    dataBytes = testData.getBytes(charset);
    sm2Coder = new SM2Coder();

    //该算法的控制参数全部在这里！！
    curve = new ECCurve.Fp(p, a, b);
    G = curve.createPoint(xg, yg);
    generateKeyPair();

  }


  private static boolean between(BigInteger param, BigInteger min, BigInteger max) {
    return (param.compareTo(min) >= 0 && param.compareTo(max) < 0);
  }

  //y^2=x^3+ax+b
  private static boolean checkPublicKey(ECPoint publicKey) {
    if (!publicKey.isInfinity()) {
      BigInteger x = publicKey.getXCoord().toBigInteger();
      BigInteger y = publicKey.getYCoord().toBigInteger();
      log.info("SM2公钥的x坐标： {}", x.toString());
      log.info("SM2公钥的x坐标-Hex： {}", Base64Coder.hexString(x.toByteArray()));
      log.info("SM2公钥的y坐标： {}", y.toString());
      log.info("SM2公钥的y坐标-Hex： {}", Base64Coder.hexString(y.toByteArray()));
      if (between(x, new BigInteger("0"), p) && between(y, new BigInteger("0"), p)) {
        BigInteger xResult = x.pow(3).add(a.multiply(x)).add(b).mod(p);
        log.info("SM2公钥的x坐标取模xResult: " + xResult.toString());
        BigInteger yResult = y.pow(2).mod(p);
        log.info("SM2公钥的y坐标取模yResult: " + yResult.toString());
        if (yResult.equals(xResult) && publicKey.multiply(n).isInfinity()) {
          return true;
        }
      }
      return false;
    } else {
      return false;
    }
  }

  public static void generateKeyPair() {
    //d 不同 - 生成的秘钥就不同
    BigInteger d = sm2Coder.random(n.subtract(new BigInteger("1")));
    publicKey = G.multiply(d).normalize();
    privateKey = d;
    if (checkPublicKey(publicKey)) {
      log.info("generate key success");
      log.info("生成的私钥为： {}", privateKey);
    } else {
      log.info("generate key failed");
    }
  }

  @Test
  public void encryptByPublicAndDecryptByPrivate()
      throws IllegalBlockSizeException, BadPaddingException,
      InvalidAlgorithmParameterException, InvalidKeyException {
    byte[] encryptData = sm2Coder.encrypt(testData, publicKey, G);
    log.info("SM2公钥加密数据base64后： {}", base64Coder.encrypt(encryptData));
    byte[] decryptData = sm2Coder.decrypt(encryptData, privateKey, curve);
    String decryptDataStr = new String(decryptData, charset);
    log.info("SM2私钥解密后数据为： {}", decryptDataStr);
    assertThat(decryptDataStr).isEqualTo(testData);
  }


}
