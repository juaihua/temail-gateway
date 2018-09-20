package com.syswin.temail.ps.client;

import com.syswin.temail.ps.common.entity.CDTPPacket;

/**
 * @author 姚华成
 * @date 2018-9-20
 */
interface TestRequestHandler {

  default CDTPPacket dispatch(CDTPPacket reqPacket) {
    return reqPacket;
  }
}