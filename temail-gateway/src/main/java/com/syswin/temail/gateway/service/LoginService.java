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
//
//    CDTPHeader header = cdtpPacket.getHeader();
//    return this.validSignature(header.getSender(),
//        header.getSignature(),
//        extractUnsignedData(cdtpPacket),
//        String.valueOf(header.getSignatureAlgorithm()));
//
//  ResponseEntity<Response> validSignature(String temail, String signature,
//      String unsignedText, String signatureAlgorithm) {
//    Map<String, String> map = new HashMap<>();
//    map.put("temail", temail);
//    map.put("unsignedBytes", unsignedText);
//    map.put("signature", signature);
//    map.put("algorithm", signatureAlgorithm);
//    String authDataJson = gson.toJson(map);
//    HttpEntity<String> requestEntity = new HttpEntity<>(authDataJson, httpHeaders);
//    try {
//      ResponseEntity<Response> responseResponseEntity = restTemplate
//          .postForEntity(authUrl, requestEntity, Response.class);
//      log.debug("signature valid result : {},  data : {}, url : {} ",
//          responseResponseEntity.getStatusCode(), authDataJson, authUrl);
//      return responseResponseEntity;
//    } catch (RestClientException e) {
//      log.error("signature valid error data : {}, url : {} ", authDataJson, authUrl, e);
//      return new ResponseEntity<>(Response.failed(SERVICE_UNAVAILABLE,
//          e.getMessage()), SERVICE_UNAVAILABLE);
//    }
//  }
//
//  private String extractUnsignedData(CDTPPacket packet) {
//    CDTPHeader header = packet.getHeader();
//    String targetAddress = defaultString(header.getTargetAddress());
//    byte[] data = packet.getData();
//    String dataSha256 = data == null ? "" : Base64.getUrlEncoder().encodeToString(DigestUtil.sha256(data));
//
//    return String.valueOf(packet.getCommandSpace() + packet.getCommand())
//        + targetAddress
//        + String.valueOf(header.getTimestamp())
//        + dataSha256;
//  }
}
