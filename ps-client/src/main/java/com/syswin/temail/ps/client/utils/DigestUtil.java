package com.syswin.temail.ps.client.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author 姚华成
 * @date 2018-03-23
 */
public class DigestUtil {

  private static final String ALGORITHM_MD5 = "MD5";
  private static final String ALGORITHM_SHA224 = "SHA-224";
  private static final String ALGORITHM_SHA256 = "SHA-256";
  private static final String ALGORITHM_SHA384 = "SHA-384";
  private static final String ALGORITHM_SHA512 = "SHA-512";

  /**
   * 对数据进行md5签名
   *
   * @param data 原始数据
   * @return 数据的签名
   */
  public static byte[] md5(byte[] data) {
    return digest(ALGORITHM_MD5, data);
  }

  /**
   * 对数据进行md5签名
   *
   * @param data 原始数据
   * @return 以16进制字符串表示的数据的签名
   */
  public static String md5AsHex(String data) {
    return HexUtil.encodeHex(md5(data.getBytes()));
  }

  /**
   * 对数据进行md5签名
   *
   * @param data 原始数据
   * @return 以Base64编码的数据的签名
   */
  public static String md5AsBase64(String data) {
    return Base64Util.encodeToString(md5(data.getBytes()));
  }

  /**
   * 对数据进行md5签名
   *
   * @param data 原始数据
   * @return 以URL安全的Base64编码的数据的签名
   */
  public static String md5AsBase64UrlSafe(String data) {
    return Base64Util.encodeToUrlSafeString(md5(data.getBytes()));
  }

  /**
   * 对数据进行sha224签名
   *
   * @param data 原始数据
   * @return 数据的签名
   */
  public static byte[] sha224(byte[] data) {
    return digest(ALGORITHM_SHA224, data);
  }

  /**
   * 对数据进行sha224签名
   *
   * @param data 原始数据
   * @return 以16进制字符串表示的数据的签名
   */
  public static String sha224AsHex(String data) {
    return HexUtil.encodeHex(sha224(data.getBytes()));
  }

  /**
   * 对数据进行sha224签名
   *
   * @param data 原始数据
   * @return 以Base64编码的数据的签名
   */
  public static String sha224AsBase64(String data) {
    return Base64Util.encodeToString(sha224(data.getBytes()));
  }

  /**
   * 对数据进行sha224签名
   *
   * @param data 原始数据
   * @return 以URL安全的Base64编码的数据的签名
   */
  public static String sha224AsBase64UrlSafe(String data) {
    return Base64Util.encodeToUrlSafeString(sha224(data.getBytes()));
  }

  /**
   * 对数据进行sha256签名
   *
   * @param data 原始数据
   * @return 数据的签名
   */
  public static byte[] sha256(byte[] data) {
    return digest(ALGORITHM_SHA256, data);
  }

  /**
   * 对数据进行sha256签名
   *
   * @param data 原始数据
   * @return 以16进制字符串表示的数据的签名
   */
  public static String sha256AsHex(String data) {
    return HexUtil.encodeHex(sha256(data.getBytes()));
  }

  /**
   * 对数据进行sha256签名
   *
   * @param data 原始数据
   * @return 以Base64编码的数据的签名
   */
  public static String sha256AsBase64(String data) {
    return Base64Util.encodeToString(sha256(data.getBytes()));
  }

  /**
   * 对数据进行sha256签名
   *
   * @param data 原始数据
   * @return 以URL安全的Base64编码的数据的签名
   */
  public static String sha256AsBase64UrlSafe(String data) {
    return Base64Util.encodeToUrlSafeString(sha256(data.getBytes()));
  }

  /**
   * 对数据进行sha384签名
   *
   * @param data 原始数据
   * @return 数据的签名
   */
  public static byte[] sha384(byte[] data) {
    return digest(ALGORITHM_SHA384, data);
  }

  /**
   * 对数据进行sha384签名
   *
   * @param data 原始数据
   * @return 以16进制字符串表示的数据的签名
   */
  public static String sha384AsHex(String data) {
    return HexUtil.encodeHex(sha384(data.getBytes()));
  }

  /**
   * 对数据进行sha384签名
   *
   * @param data 原始数据
   * @return 以Base64编码的数据的签名
   */
  public static String sha384AsBase64(String data) {
    return Base64Util.encodeToString(sha384(data.getBytes()));
  }

  /**
   * 对数据进行sha384签名
   *
   * @param data 原始数据
   * @return 以URL安全的Base64编码的数据的签名
   */
  public static String sha384AsBase64UrlSafe(String data) {
    return Base64Util.encodeToUrlSafeString(sha384(data.getBytes()));
  }

  /**
   * 对数据进行sha512签名
   *
   * @param data 原始数据
   * @return 数据的签名
   */
  public static byte[] sha512(byte[] data) {
    return digest(ALGORITHM_SHA512, data);
  }

  /**
   * 对数据进行sha512签名
   *
   * @param data 原始数据
   * @return 以16进制字符串表示的数据的签名
   */
  public static String sha512AsHex(String data) {
    return HexUtil.encodeHex(sha512(data.getBytes()));
  }

  /**
   * 对数据进行sha512签名
   *
   * @param data 原始数据
   * @return 以Base64编码的数据的签名
   */
  public static String sha512AsBase64(String data) {
    return Base64Util.encodeToString(sha512(data.getBytes()));
  }

  /**
   * 对数据进行sha512签名
   *
   * @param data 原始数据
   * @return 以URL安全的Base64编码的数据的签名
   */
  public static String sha512AsBase64UrlSafe(String data) {
    return Base64Util.encodeToUrlSafeString(sha512(data.getBytes()));
  }

  private static MessageDigest getDigest(String algorithm) {
    try {
      return MessageDigest.getInstance(algorithm);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("Could not find MessageDigest with algorithm \"" + algorithm + "\"", ex);
    }
  }

  public static byte[] digest(String algorithm, byte[] data) {
    return getDigest(algorithm).digest(data);
  }

  public static void main(String[] args) {
    System.out.println(HexUtil.encodeHex(DigestUtil.sha256(new byte[0])));
  }
}
