package com.syswin.temail.ps.common.codec.decrypt;

import static com.syswin.temail.ps.common.entity.DataEncryptType.ECC_RECEIVER_PUB;
import static com.syswin.temail.ps.common.entity.DataEncryptType.ECC_SENDER_PUB;
import static com.syswin.temail.ps.common.entity.DataEncryptType.NONE;

import com.syswin.temail.kms.vault.KeyAwareAsymmetricCipher;
import com.syswin.temail.ps.common.codec.SimpleBodyExtractor;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.DataEncryptType;
import java.util.HashMap;
import java.util.Map;

public class AutoDecryptBodyExtractor extends SimpleBodyExtractor {

  private Map<DataEncryptType, DataDecryptor> decryptorMap = new HashMap<>();

  public AutoDecryptBodyExtractor(KeyAwareAsymmetricCipher cipher) {
    // 本构造函数仅用于测试
    initDecryptor(cipher);
  }

  public AutoDecryptBodyExtractor(String vaultRegistryUrl, String tenantId) {
    initDecryptor(vaultRegistryUrl, tenantId);
  }

  @Override
  public void decrypt(CDTPPacket packet) {
    CDTPHeader cdtpHeader = packet.getHeader();
    if (cdtpHeader != null) {
      DataEncryptType dataEncryptType = DataEncryptType.valueOf(cdtpHeader.getDataEncryptionMethod());
      DataDecryptor dataDecryptor = decryptorMap.get(dataEncryptType);
      dataDecryptor.decrypt(packet);
    }
  }

  private void initDecryptor(String vaultRegistryUrl, String tenantId) {
    decryptorMap.put(NONE, new NoneDataDecryptor());
//    decryptorMap.put(RSA_RECEIVER_PUB, new RsaReceiverDataDecryptor(vaultRegistryUrl,tenantId));
//    decryptorMap.put(RSA_SENDER_PUB, new RsaSenderDataDecryptor(vaultRegistryUrl,tenantId));
//    decryptorMap.put(AES_CBC_32, new AesDataDecryptor());
    decryptorMap.put(ECC_RECEIVER_PUB, new EccReceiverDataDecryptor(vaultRegistryUrl, tenantId));
    decryptorMap.put(ECC_SENDER_PUB, new EccSenderDataDecryptor(vaultRegistryUrl, tenantId));
  }

  private void initDecryptor(KeyAwareAsymmetricCipher cipher) {
    decryptorMap.put(NONE, new NoneDataDecryptor());
//    decryptorMap.put(RSA_RECEIVER_PUB, new RsaReceiverDataDecryptor(vaultRegistryUrl,tenantId));
//    decryptorMap.put(RSA_SENDER_PUB, new RsaSenderDataDecryptor(vaultRegistryUrl,tenantId));
//    decryptorMap.put(AES_CBC_32, new AesDataDecryptor());
    decryptorMap.put(ECC_RECEIVER_PUB, new EccReceiverDataDecryptor(cipher));
    decryptorMap.put(ECC_SENDER_PUB, new EccSenderDataDecryptor(cipher));
  }

}
