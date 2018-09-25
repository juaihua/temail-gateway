package com.syswin.temail.ps.client;

/**
 * @author 姚华成
 * @date 2018-9-25
 */
interface Signer {

  /**
   * 根据用户ID获取对应密钥进行签名
   *
   * @param userId 账户ID e.g. temail地址
   * @param plaintext 用于签名的明文
   * @return 明文对应的签名Base64编码
   */
  String sign(String userId, String plaintext);

  /**
   * 获取当前签名生成器使用的签名算法
   *
   * @return 签名算法的编号
   */
  int getAlgorithm();
}
