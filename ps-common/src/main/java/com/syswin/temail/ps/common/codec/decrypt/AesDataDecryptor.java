package com.syswin.temail.ps.common.codec.decrypt;

import com.syswin.temail.ps.common.entity.CDTPPacket;

/**
 * @author 姚华成
 * @date 2018-10-19
 */
public class AesDataDecryptor implements DataDecryptor {

  @Override
  public void decrypt(CDTPPacket packet) {
    throw new UnsupportedOperationException("暂时不支持AES加密算法！");
  }
}
