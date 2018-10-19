package com.syswin.temail.ps.common.codec.decrypt;

import com.syswin.temail.ps.common.entity.CDTPPacket;

/**
 * @author 姚华成
 * @date 2018-10-19
 */
public interface DataDecryptor {

  void decrypt(CDTPPacket packet);
}
