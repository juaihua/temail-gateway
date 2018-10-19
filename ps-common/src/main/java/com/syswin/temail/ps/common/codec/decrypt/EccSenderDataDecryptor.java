package com.syswin.temail.ps.common.codec.decrypt;

import com.syswin.temail.ps.common.entity.CDTPPacket;

/**
 * @author 姚华成
 * @date 2018-10-19
 */
public class EccSenderDataDecryptor extends EccDataDecryptor {

  public EccSenderDataDecryptor(String vaultRegistryUrl, String tenantId) {
    super(vaultRegistryUrl, tenantId);
  }

  @Override
  protected String getUserId(CDTPPacket packet) {
    return packet.getHeader().getSender();
  }
}
