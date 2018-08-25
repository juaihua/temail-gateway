package com.syswin.temail.gateway.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 姚华成
 * @date 2018-8-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session {
  private String temail;
  private String deviceId;
}
