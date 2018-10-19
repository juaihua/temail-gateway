package com.syswin.temail.ps.common.codec.decrypt;

import com.syswin.temail.ps.common.entity.CDTPPacket;

/**
 * @author 姚华成
 * @date 2018-10-19
 */
public class EccReceiverDataDecryptor extends EccDataDecryptor {

  public EccReceiverDataDecryptor(String vaultRegistryUrl, String tenantId) {
    super(vaultRegistryUrl, tenantId);
  }

  @Override
  protected String getUserId(CDTPPacket packet) {
    return packet.getHeader().getReceiver();
  }
}

