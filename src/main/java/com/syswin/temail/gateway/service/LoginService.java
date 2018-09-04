package com.syswin.temail.gateway.service;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.syswin.temail.gateway.entity.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

@Slf4j
class LoginService {

  private final RestTemplate restTemplate;
  private final String authUrl;
  private final Gson gson;
  private final HttpHeaders httpHeaders = new HttpHeaders();


  public LoginService(RestTemplate restTemplate, String authUrl) {
    this.restTemplate = restTemplate;
    this.authUrl = authUrl;
    this.gson = new Gson();
    httpHeaders.setContentType(APPLICATION_JSON_UTF8);
  }


  public ResponseEntity<Response> validSignature(String temail, String signature, String unsignedText, String signatureAlgorithm) {
    Map<String, String> map = new HashMap<>();
    map.put("temail", temail);
    map.put("unsignedBytes", unsignedText);
    map.put("signature", signature);
    map.put("signatureAlgorithm",signatureAlgorithm);
    String authDataJson = gson.toJson(map);
    HttpEntity<String> requestEntity = new HttpEntity<>(authDataJson, httpHeaders);
    try {
      return restTemplate
          .postForEntity(authUrl, requestEntity, Response.class);
    } catch (RestClientException e) {
      log.error("Failed to reach remote auth service at {}", authUrl, e);
      return new ResponseEntity<>(Response.failed(SERVICE_UNAVAILABLE, e.getMessage()), SERVICE_UNAVAILABLE);
    }
  }

}
