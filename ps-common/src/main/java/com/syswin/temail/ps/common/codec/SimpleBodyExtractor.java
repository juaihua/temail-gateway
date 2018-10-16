package com.syswin.temail.ps.common.codec;

import static com.syswin.temail.kms.vault.CipherAlgorithm.ECDSA;

import com.syswin.temail.kms.vault.KeyAwareAsymmetricCipher;
import com.syswin.temail.kms.vault.VaultKeeper;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.DataEncryptType;
import com.syswin.temail.ps.common.exception.PacketException;
import io.netty.buffer.ByteBuf;
import java.util.Base64;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SimpleBodyExtractor implements BodyExtractor {

  private KeyAwareAsymmetricCipher cipher;

  public SimpleBodyExtractor(KeyAwareAsymmetricCipher cipher) {
    this.cipher = cipher;
  }

  public SimpleBodyExtractor(String vaultRegistryUrl, String tenantId) {
    cipher = VaultKeeper.keyAwareVault(vaultRegistryUrl, tenantId).asymmetricCipher(ECDSA);
  }

  @Override
  public byte[] fromBuffer(short commandSpace, short command, ByteBuf byteBuf, int remainingBytes) {
    byte[] data = new byte[remainingBytes];
    byteBuf.readBytes(data);
    return data;
  }

  @Override
  public void decrypt(CDTPPacket packet) {
    CDTPHeader cdtpHeader = packet.getHeader();
    if (cdtpHeader != null) {
      DataEncryptType dataEncryptType = DataEncryptType.valueOf(cdtpHeader.getDataEncryptionMethod());
      String temail;
      switch (dataEncryptType) {
        case NONE:
          temail = null;
          break;
        case ECC_RECEIVER_PUB:
          temail = cdtpHeader.getReceiver();
          break;
        case ECC_SENDER_PUB:
          temail = cdtpHeader.getSender();
          break;
        default:
          // TODO 暂不支持其他的加密算法
          temail = null;
      }
      if (temail != null) {
        if (cipher == null) {
          throw new PacketException("配置为自动解密但没有设置");
        }

        String base64Data = Base64.getUrlEncoder().encodeToString(packet.getData());
        String decryptedData = cipher.decrypt(temail, base64Data);
        packet.setData(Base64.getUrlDecoder().decode(decryptedData));
      }
    }

  }

}
