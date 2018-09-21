package com.syswin.temail.ps.common.utils;

import com.syswin.temail.ps.common.entity.CDTPPacket;

/**
 * @author 姚华成
 * @date 2018-9-20
 */
public class SignatureUtil {

  public static void genSignature(CDTPPacket packet) {
//    try {
//      CDTPHeader header = packet.getHeader();
//      byte[] dataSha256 = MessageDigest.getInstance("SHA-256").digest(packet.getData());
//      String unsigned =
//          String.valueOf(packet.getCommandSpace() + packet.getCommand()) + header.getTargetAddress() + String
//              .valueOf(header.getTimestamp()) + Base64.getEncoder().encodeToString(dataSha256);
//      String temail = header.getSender();
//      String sign = Base64.getEncoder().encodeToString(cipher.sign(temail, unsigned));
//      header.setSignatureAlgorithm(ECC512_CODE);
//      header.setSignature(sign);
//    } catch (NoSuchAlgorithmException e) {
//      throw new PsClientException("对数据进行签名时出错！", e);
//    }

  }

}
