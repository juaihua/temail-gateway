package com.syswin.temail.ps.common.codec.decrypt;

import com.syswin.temail.kms.vault.CipherAlgorithm;

/**
 * @author 姚华成
 * @date 2018-10-19
 */
public abstract class EccDataDecryptor extends AbstractDataDecryptor {

  public EccDataDecryptor(String vaultRegistryUrl, String tenantId) {
    super(vaultRegistryUrl, tenantId);
  }

  @Override
  protected CipherAlgorithm getCryptAlgorithm() {
    return CipherAlgorithm.ECDSA;
  }
}

