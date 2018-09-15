package com.syswin.temail.gateway.encrypt.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;

public class SHA256Coder {

  private final MessageDigest messageDigest;
  private final Encoder encoder = Base64.getEncoder();

  public SHA256Coder(){
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  public byte[] encrypt(byte[] data){
    messageDigest.update(data);
    return messageDigest.digest();
  }

  public String encryptAndSwitch2Base64(byte[] data){
    return encoder.encodeToString(this.encrypt(data));
  }

}