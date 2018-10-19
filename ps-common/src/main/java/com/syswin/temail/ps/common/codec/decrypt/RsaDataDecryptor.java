package com.syswin.temail.ps.common.codec.decrypt;

import com.syswin.temail.kms.vault.CipherAlgorithm;

/**
 * @author 姚华成
 * @date 2018-10-19
 */
public abstract class RsaDataDecryptor extends AbstractDataDecryptor {

  public RsaDataDecryptor(String vaultRegistryUrl, String tenantId) {
    super(vaultRegistryUrl, tenantId);
  }

  @Override
  protected CipherAlgorithm getCryptAlgorithm() {
    throw new UnsupportedOperationException("暂时不支持RSA加密算法！");
  }
}
