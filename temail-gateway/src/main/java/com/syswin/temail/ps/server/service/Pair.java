package com.syswin.temail.ps.server.service;

import com.syswin.temail.gateway.entity.TemailAccoutLocations;
import com.syswin.temail.ps.server.service.RemoteStatusService.TemailAcctUptOptType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
class Pair {
  private final TemailAcctUptOptType temailAcctUptOptType;
  private final TemailAccoutLocations temailAccoutLocations;
}
