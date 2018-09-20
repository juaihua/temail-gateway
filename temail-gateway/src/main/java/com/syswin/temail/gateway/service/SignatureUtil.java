package com.syswin.temail.gateway.service;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.SignatureAlgorithm;

/**
 * @author 姚华成
 * @date 2018-9-20
 */
public class SignatureUtil {

  public static void resetSignature(CDTPPacket reqPacket) {
    // todo(姚华成) 当前验签功能没有实现，所以把加密标识清除
    // 请求的数据可能加密，而返回的数据没有加密，需要设置加密标识
    reqPacket.getHeader().setSignatureAlgorithm(SignatureAlgorithm.NONE_CODE);
    reqPacket.getHeader().setSignature(null);
  }

}
