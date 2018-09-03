package com.syswin.temail.gateway.encrypt.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Coder {

  private MessageDigest messageDigest;

  private Base64Coder base64Coder;

  public SHA256Coder(){
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
      base64Coder = new Base64Coder();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
  }

  public byte[] encrypt(byte[] data){
    messageDigest.update(data);
    return messageDigest.digest();
  }

  public String encryptAndSwitch2Base64(byte[] data){
    return base64Coder.encrypt(this.encrypt(data));
  }

}