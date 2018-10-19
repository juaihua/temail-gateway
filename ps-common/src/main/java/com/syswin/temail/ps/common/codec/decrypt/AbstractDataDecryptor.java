package com.syswin.temail.ps.common.codec.decrypt;

import com.syswin.temail.kms.vault.CipherAlgorithm;
import com.syswin.temail.kms.vault.KeyAwareAsymmetricCipher;
import com.syswin.temail.kms.vault.VaultKeeper;
import com.syswin.temail.ps.common.entity.CDTPPacket;

/**
 * @author 姚华成
 * @date 2018-10-19
 */
public abstract class AbstractDataDecryptor implements DataDecryptor {

  protected KeyAwareAsymmetricCipher cipher;

  protected AbstractDataDecryptor(KeyAwareAsymmetricCipher cipher) {
    this.cipher = cipher;
  }

  protected AbstractDataDecryptor(String vaultRegistryUrl, String tenantId) {
    cipher = VaultKeeper.keyAwareVault(vaultRegistryUrl, tenantId).asymmetricCipher(getCryptAlgorithm());
  }

  public void decrypt(CDTPPacket packet) {
    String data = new String(packet.getData());
    String decryptedData = cipher.decrypt(getUserId(packet), data);
    packet.setData(decryptedData.getBytes());
  }

  protected abstract CipherAlgorithm getCryptAlgorithm();

  protected abstract String getUserId(CDTPPacket packet);

}
