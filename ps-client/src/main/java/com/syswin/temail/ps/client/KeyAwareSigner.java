package com.syswin.temail.ps.client;

import static com.syswin.temail.kms.vault.CipherAlgorithm.ECDSA;
import static com.syswin.temail.ps.common.entity.SignatureAlgorithm.ECC512_CODE;

import com.syswin.temail.kms.vault.KeyAwareAsymmetricCipher;
import com.syswin.temail.kms.vault.VaultKeeper;

/**
 * @author 姚华成
 * @date 2018-9-25
 */
class KeyAwareSigner implements Signer {

  private final KeyAwareAsymmetricCipher cipher;

  public KeyAwareSigner(String vaultRegistryUrl, String tenantId) {
    cipher = VaultKeeper.keyAwareVault(vaultRegistryUrl, tenantId).asymmetricCipher(ECDSA);
  }

  @Override
  public String sign(String userId, String plaintext) {
    return cipher.sign(userId, plaintext);
  }

  @Override
  public int getAlgorithm() {
    return ECC512_CODE;
  }
}
