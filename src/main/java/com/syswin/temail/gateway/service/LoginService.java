package com.syswin.temail.gateway.service;

import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
class LoginService {

  private final RestTemplate restTemplate;

  private final String authUrl;

  public LoginService(RestTemplate restTemplate, String authUrl) {
    this.restTemplate = restTemplate;
    this.authUrl = authUrl;
  }

  public ResponseEntity<Response> validSignature(CDTPPacket cdtpPacket) {
    CDTPPacketTrans packetTrans = new CDTPPacketTrans(cdtpPacket);
    HttpEntity<CDTPPacketTrans> requestEntity = new HttpEntity<>(packetTrans);
    return restTemplate.postForEntity(authUrl, requestEntity, Response.class);
  }
}
