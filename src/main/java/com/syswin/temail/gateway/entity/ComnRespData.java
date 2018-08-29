package com.syswin.temail.gateway.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class ComnRespData {

  private String msg;

  private boolean isSucess;

  public ComnRespData(boolean isSuccess) {
    this.isSucess = isSuccess;
    this.msg = isSuccess ? "" : "fail";
  }

}