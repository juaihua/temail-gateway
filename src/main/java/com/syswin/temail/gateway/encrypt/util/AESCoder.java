package com.syswin.temail.gateway.encrypt.util;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by juaihua on 2018/9/2.
 */
public class AESCoder {

  public static final String ALGORITHM = "AES";

  /**
   * 提取秘钥数据
   *
   * @param key
   * @return
   */
  public SecretKey extractKey(byte[] keyBytes) {
    return new SecretKeySpec(keyBytes, ALGORITHM);
  }


  /**
   * encrypt data by aes
   *
   * @param key
   * @param data
   * @return
   */
  public byte[] encrypt(byte[] keyBytes, byte[] data)
      throws InvalidKeyException, NoSuchPaddingException,
      NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException {
    Key key = extractKey(keyBytes);
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    return cipher.doFinal(data);
  }


  /**
   * decrypt data by aes
   *
   * @param key
   * @param encryData
   * @return
   */
  public byte[] decrypt(byte[] keyBytes, byte[] encryData)
      throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException,
      NoSuchPaddingException, NoSuchAlgorithmException {
    Key key = extractKey(keyBytes);
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.DECRYPT_MODE, key);
    return cipher.doFinal(encryData);
  }

}
