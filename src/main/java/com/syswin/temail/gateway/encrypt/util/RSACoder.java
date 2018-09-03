package com.syswin.temail.gateway.encrypt.util;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import lombok.extern.slf4j.Slf4j;

import static java.security.Signature.getInstance;


@Slf4j
public class RSACoder {

  public static final String KEY_ALGORITHM = "RSA";

  /**
   * gene privateKey
   *
   * @param privateKeyBytes
   * @return
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   */
  private PrivateKey extractPrivateKey(byte[] privateKeyBytes)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
    return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
  }

  /**
   * gene publicKey
   *
   * @param publicKeyBytes
   * @return
   */
  private PublicKey extractPublicKey(byte[] publicKeyBytes) throws
      InvalidKeySpecException, NoSuchAlgorithmException {
    X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKeyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
    return keyFactory.generatePublic(x509EncodedKeySpec);
  }

  /**
   * sign data by priavteKey
   *
   * @param dataBytes
   * @param privateKeyBytes
   * @return
   */
  public byte[] signData(byte[] privateKeyBytes,
      byte[] dataBytes, String sigatureAlgorithm)
      throws NoSuchAlgorithmException, InvalidKeySpecException,
      InvalidKeyException, SignatureException {
    PrivateKey privateKey = extractPrivateKey(privateKeyBytes);
    Signature signature = getInstance(sigatureAlgorithm);
    signature.initSign(privateKey);
    signature.update(dataBytes);
    return signature.sign();
  }

  /**
   * this method will be used for valia signature
   *
   * @param oriData
   * @param publicKeyBytes
   * @return
   */
  public boolean validSign(byte[] publicKeyBytes, byte[] oriData,
      byte[] signedData, String sigatureAlgorithm)
      throws NoSuchAlgorithmException, InvalidKeyException,
      SignatureException, InvalidKeySpecException {
    PublicKey publicKey = extractPublicKey(publicKeyBytes);
    Signature signature = getInstance(sigatureAlgorithm);
    signature.initVerify(publicKey);
    signature.update(oriData);
    return signature.verify(signedData);
  }

  /**
   * encry data by PrivateKey
   *
   * @param privateKeyBytes
   * @param testDataBytes
   * @return
   */
  public byte[] encryptByPrivate(byte[] privateKeyBytes, byte[] data)
      throws InvalidKeySpecException, NoSuchAlgorithmException,
      NoSuchPaddingException, InvalidKeyException,
      BadPaddingException, IllegalBlockSizeException {
    PrivateKey privateKey = extractPrivateKey(privateKeyBytes);
    Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
    cipher.init(Cipher.ENCRYPT_MODE, privateKey);
    return cipher.doFinal(data);
  }


  /**
   * decrypt data by privateKey
   *
   * @param privateKeyBytes
   * @param encryptData
   * @return
   */
  public byte[] decryptByPrivate(byte[] privateKeyBytes, byte[] encryptData)
      throws InvalidKeySpecException, NoSuchAlgorithmException,
      NoSuchPaddingException, InvalidKeyException,
      BadPaddingException, IllegalBlockSizeException {
    PrivateKey privateKey = extractPrivateKey(privateKeyBytes);
    Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
    cipher.init(Cipher.DECRYPT_MODE, privateKey);
    return cipher.doFinal(encryptData);
  }


  /**
   * encrypt data by public key
   *
   * @param publicKeyBytes
   * @param data
   * @return
   */
  public byte[] encryptByPublic(byte[] publicKeyBytes, byte[] data)
      throws InvalidKeySpecException, NoSuchAlgorithmException,
      NoSuchPaddingException, InvalidKeyException,
      BadPaddingException, IllegalBlockSizeException {
    PublicKey publicKey = extractPublicKey(publicKeyBytes);
    Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    return cipher.doFinal(data);
  }


   /**
   * decrypt data by public key
   *
   * @param publicKeyBytes
   * @param encryptData
   * @return
   */
  public byte[] decryptByPublic(byte[] publicKeyBytes, byte[] encryptData)
      throws NoSuchPaddingException, NoSuchAlgorithmException,
      InvalidKeyException, BadPaddingException,
      IllegalBlockSizeException, InvalidKeySpecException {
    PublicKey publicKey = extractPublicKey(publicKeyBytes);
    Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
    cipher.init(Cipher.DECRYPT_MODE, publicKey);
    return cipher.doFinal(encryptData);
  }

}