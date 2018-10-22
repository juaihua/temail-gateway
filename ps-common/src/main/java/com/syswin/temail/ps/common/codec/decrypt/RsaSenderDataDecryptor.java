package com.syswin.temail.ps.common.codec.decrypt;

import com.syswin.temail.kms.vault.KeyAwareAsymmetricCipher;
import com.syswin.temail.ps.common.entity.CDTPPacket;

/**
 * @author 姚华成
 * @date 2018-10-19
 */
class RsaSenderDataDecryptor extends RsaDataDecryptor {

  public RsaSenderDataDecryptor(KeyAwareAsymmetricCipher cipher) {
    super(cipher);
  }

  public RsaSenderDataDecryptor(String vaultRegistryUrl, String tenantId) {
    super(vaultRegistryUrl, tenantId);
  }

  @Override
  protected String getUserId(CDTPPacket packet) {
    return packet.getHeader().getSender();
  }
}