package com.syswin.temail.cdtpserver.status;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import com.google.gson.Gson;
import com.syswin.temail.cdtpserver.entity.TemailSocketInfo;
import com.syswin.temail.cdtpserver.entity.TemailSocketResponse;
import com.syswin.temail.cdtpserver.handler.SilentResponseErrorHandler;
import com.syswin.temail.cdtpserver.properties.TemailServerProperties;

// 更新Temail信息到远程状态服务信息
@Component
@Slf4j
public class TemailSocketSyncClient {

  @Resource
  TemailServerProperties temailServerConfig;

  public void updateTemailSocketInfToRemote(TemailSocketInfo temailSocketInfo) {
    try {
      log.info("更新TemailSocket 信息到状态服务:{}", temailSocketInfo.toString());

      Gson gson = new Gson();
      String socketChannelStatusJson = gson.toJson(temailSocketInfo);

      Mono<TemailSocketResponse> monoResp =
          WebClient.create().post().uri(temailServerConfig.getUpdateSocketStatusUrl())
              .contentType(MediaType.APPLICATION_JSON_UTF8)
              .body(BodyInserters.fromObject(socketChannelStatusJson)).retrieve()
              .bodyToMono(TemailSocketResponse.class);

      TemailSocketResponse temailSocketResponse = monoResp.block();
      if (StringUtils.isNotEmpty(temailSocketResponse.getResult())
          && temailSocketResponse.getResult().equalsIgnoreCase("success")) {
        log.info("更新TemailSocket 信息到状态服务成功, Temail Socket is {} ", temailSocketResponse.toString());
      } else {
        log.error("****更新TemailSocket 信息到状态服务失败, Temail Socket is {} ",
            temailSocketResponse.toString());
      }

    } catch (Exception ex) {
      log.error("更新TemailSocketInfo 到远程服务器失败, temailSocketInfo is {}", temailSocketInfo, ex);
    }


  }
}