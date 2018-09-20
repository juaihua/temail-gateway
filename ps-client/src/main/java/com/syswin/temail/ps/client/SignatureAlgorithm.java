package com.syswin.temail.ps.client;

import lombok.Getter;

/**
 * @author 姚华成
 * @date 2018-9-20
 */
@Getter
public enum SignatureAlgorithm {
  RSA2048(1),
  ECC512(2),
  SM2(3),
  ;
  public static final byte RSA2048_CODE = RSA2048.code;
  public static final byte ECC512_CODE = ECC512.code;
  public static final byte SM2_CODE = SM2.code;
  private final byte code;

  SignatureAlgorithm(int code) {
    this.code = (byte) code;
  }

  public static SignatureAlgorithm valueOf(int code) {
    byte byteCode = (byte) code;
    for (SignatureAlgorithm value : values()) {
      if (value.code == byteCode) {
        return value;
      }
    }
    throw new PsClientException("不支持的签名算法：" + code);
  }
}
