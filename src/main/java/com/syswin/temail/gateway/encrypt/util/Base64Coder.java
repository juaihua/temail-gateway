package com.syswin.temail.gateway.encrypt.util;


import java.nio.charset.Charset;
import java.util.Base64;

public class Base64Coder {

  public final Charset CHARSET = Charset.forName("utf-8");

  private final Base64.Encoder ENCODER = Base64.getEncoder();

  private final Base64.Decoder DECODER = Base64.getDecoder();

  public  String encrypt(String data, Charset chaset){
    return encrypt(data.getBytes(chaset));
  }

  public  String encrypt(String data){
    return encrypt(data.getBytes(CHARSET));
  }

  public  String encrypt(byte[] data){
    return ENCODER.encodeToString(data);
  }

  public  byte[] decrypt(String base64Str) throws Exception{
    return DECODER.decode(base64Str);
  }

  public static String hexString(byte[] data){
    StringBuilder sbd = new StringBuilder();
    for(int i = 0; i < data.length; i++){
      String hexStr = Integer.toHexString((0xFF & data[i]));
      if(hexStr.length() == 1){hexStr = "0"+hexStr;}
      sbd.append(hexStr);
    }
    return sbd.toString();
  }

}
