package com.syswin.temail.ps.client;

/**
 * @author 姚华成
 * @date 2018-9-19
 */
class MessageMaker {

  static Message sendSingleChatMessage(String sender, String receiver, String content) {
    return MessageConverter.fromCDTPPacket(PacketMaker.sendSingleCharPacket(sender, receiver, content));
  }
}
