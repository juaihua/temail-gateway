package com.syswin.temail.gateway.encrypt.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NullCipher;

import sun.security.ec.ECKeyFactory;
import sun.security.pkcs.PKCS8Key;
import sun.security.x509.X509Key;

public class ECCCoder {

  public ECPrivateKey extractPrivateKeyBytes(byte[] privateKeyBytes) throws InvalidKeyException {
    PKCS8Key pkcs8Key = new PKCS8Key();
    pkcs8Key.decode(privateKeyBytes);
    return (ECPrivateKey) ECKeyFactory.toECKey(pkcs8Key);
  }

  public ECPublicKey extractPublicKeyBytes(byte[] publicKeyBytes) throws InvalidKeyException {
    X509Key x509Key = new X509Key();
    x509Key.decode(publicKeyBytes);
    return (ECPublicKey) ECKeyFactory.toECKey(x509Key);
  }

  public byte[] encryptByPrivate(byte[] privateKeyBytes, byte[] dataBytes)
      throws InvalidKeyException, InvalidAlgorithmParameterException,
      BadPaddingException, IllegalBlockSizeException {
    ECPrivateKey ecPrivateKey = extractPrivateKeyBytes(privateKeyBytes);
    ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(ecPrivateKey.getS(), ecPrivateKey.getParams());
    Cipher cipher = new NullCipher();
    cipher.init(Cipher.ENCRYPT_MODE, ecPrivateKey, ecPrivateKeySpec.getParams());
    return cipher.doFinal(dataBytes);
  }

  public byte[] decryptByPrivate(byte[] privateKeyBytes, byte[] encryptData)
      throws InvalidKeyException, InvalidAlgorithmParameterException,
      BadPaddingException, IllegalBlockSizeException {
    ECPrivateKey ecPrivateKey = extractPrivateKeyBytes(privateKeyBytes);
    ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(ecPrivateKey.getS(), ecPrivateKey.getParams());
    Cipher cipher = new NullCipher();
    cipher.init(Cipher.DECRYPT_MODE, ecPrivateKey, ecPrivateKeySpec.getParams());
    return cipher.doFinal(encryptData);
  }

  public byte[] encryptByPublic(byte[] publicKeyBytes, byte[] dataBytes)
      throws InvalidKeyException, InvalidAlgorithmParameterException,
      BadPaddingException, IllegalBlockSizeException {
    ECPublicKey ecPublicKey = extractPublicKeyBytes(publicKeyBytes);
    ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(ecPublicKey.getW(), ecPublicKey.getParams());
    Cipher cipher = new NullCipher();
    cipher.init(Cipher.ENCRYPT_MODE, ecPublicKey, ecPublicKeySpec.getParams());
    return cipher.doFinal(dataBytes);
  }

  public byte[] decryptByPublic(byte[] publicKeyBytes, byte[] encryptData)
      throws BadPaddingException, IllegalBlockSizeException,
      InvalidAlgorithmParameterException, InvalidKeyException {
    ECPublicKey ecPublicKey = extractPublicKeyBytes(publicKeyBytes);
    ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(ecPublicKey.getW(), ecPublicKey.getParams());
    Cipher cipher = new NullCipher();
    cipher.init(Cipher.DECRYPT_MODE, ecPublicKey, ecPublicKeySpec.getParams());
    return cipher.doFinal(encryptData);
  }

}
