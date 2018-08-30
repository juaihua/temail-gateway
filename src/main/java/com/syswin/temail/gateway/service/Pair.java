package com.syswin.temail.gateway.service;

import com.syswin.temail.gateway.entity.TemailAcctStses;
import com.syswin.temail.gateway.service.RemoteStatusService.TemailAcctUptOptType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
class Pair {
  private final TemailAcctUptOptType temailAcctUptOptType;
  private final TemailAcctStses temailAcctStses;
}
