package com.syswin.temail.cdtpserver.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * @author 姚华成
 * @date 2018/8/8
 */
@Data
public class CDTPBody implements Serializable {

  private Map<String, String> header;
  private Map<String, String> query;
  private Map<String, Object> body;
}
