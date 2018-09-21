package com.syswin.temail.gateway.grpc;

import com.syswin.temail.channel.grpc.servers.ChannelLocation;
import com.syswin.temail.channel.grpc.servers.ChannelLocationes;
import com.syswin.temail.channel.grpc.servers.GatewayServer;
import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.entity.TemailAccoutLocations;
import java.util.function.Consumer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * in wrapper we add reconnect and heartBeat logic and keep original client clean.<p>
 * be aware of keeping client fail fast when channel-server is not avalilable
 * , and my be we can update the channel in batch for this fail fast afterwards ...
 */
@Slf4j
@Data
//@Component
public class GrpcClientWrapper implements GrpcClient, GrpcStatusAdapter {

  private TemailGatewayProperties temailGatewayProperties;

  private volatile boolean serviceIsAvaliable = false;

  private GrpcReconnectManager grpcReconnectManager;

  private GrpcHeartBeatManager grpcHeartBeatManager;

  private GrpcClient grpcClient;

  private GatewayServer curServerInfo;

  private final String host;

  private final int port;

  @Autowired
  public GrpcClientWrapper(TemailGatewayProperties temailGatewayProperties) {
    this.temailGatewayProperties = temailGatewayProperties;
    this.host = temailGatewayProperties.getGrpcServerHost();
    this.port = Integer.valueOf(temailGatewayProperties.getGrpcServerPort());
    this.grpcClient = new GrpcClientImpl(host, port);
    this.curServerInfo = GatewayServer.newBuilder()
        .setProcessId(temailGatewayProperties.getInstance().getProcessId())
        .setIp(temailGatewayProperties.getInstance().getHostOf()).build();
    this.grpcReconnectManager = new GrpcReconnectManager(this, temailGatewayProperties);
    this.grpcHeartBeatManager = new GrpcHeartBeatManager(this, temailGatewayProperties);
    serviceIsAvaliable = true;
  }


  /**
   *  register and start heartBeat for default current server
   */
  public void startClient() {
    this.serverRegistry(curServerInfo);
    grpcHeartBeatManager.heartBeat(new Consumer<Boolean>() {
      @Override
      public void accept(Boolean aBoolean) {
        if (!aBoolean.booleanValue()) {
          // if heartBean fail, that means grpcServer is unavailible
          // and if the serviceIsAvaliable is true means need to reconnect!
          if (serviceIsAvaliable) {
            reconnect();
          } else {
            log.info("grpc server not availible, reconnect is beging executed. wait.");
          }
        }
      }
    });
  }


  @Override
  public boolean serverRegistry(GatewayServer gatewayServer) {
    try {
      if (!serviceIsAvaliable) {
        log.warn("server registry fail, cause grpcServer is unavalilable, reconnect logic is being executed.");
        return false;
      }
      return grpcClient.serverRegistry(gatewayServer);
    } catch (Exception e) {
      log.error("server registry fail, try to reconnect grpcServer.", e);
      reconnect();
      return false;
    }
  }


  @Override
  public boolean serverHeartBeat(GatewayServer gatewayServer) {
    try {
      if (!serviceIsAvaliable) {
        log.warn("server heart beat fail, cause grpcServer is unavalilable, reconnect logic is being executed.");
        return false;
      }
      return grpcClient.serverHeartBeat(gatewayServer);
    } catch (Exception e) {
      log.error("server heart beat fail, try to reconnect grpcServer.", e);
      reconnect();
      return false;
    }
  }


  @Override
  public boolean syncChannelLocationes(ChannelLocationes channelLocationes) {
    try {
      if (!serviceIsAvaliable) {
        log.warn("sync channel locationes fail, cause grpcServer is unavalilable, reconnect logic is being executed.");
        return false;
      }
      log.info("sync channel locationes success : {}", channelLocationes.toString());
      return grpcClient.syncChannelLocationes(channelLocationes);
    } catch (Exception e) {
      log.error("sync channel locationes fail : {} ", channelLocationes.toString(), e);
      reconnect();
      return false;
    }
  }


  @Override
  public boolean removeChannelLocationes(ChannelLocationes channelLocationes) {
    try {
      if (!serviceIsAvaliable) {
        log.warn(
            "remove channel locationes fail, cause grpcServer is unavalilable, reconnect logic is being executed.");
        return false;
      }
      log.info("remove channel locationes success : {} - success. ", channelLocationes.toString());
      return grpcClient.removeChannelLocationes(channelLocationes);
    } catch (Exception e) {
      log.error("remove channel locationes fail : {} ", channelLocationes.toString(), e);
      reconnect();
      return false;
    }
  }


  /**
   * reconnect client, once success then registry the server info again
   */
  public void reconnect() {
    try {
      log.info("grpc client is unavailible, try to reconncet again!");
      serviceIsAvaliable = false;
      grpcReconnectManager.reconnect(new Consumer<Boolean>() {
        @Override
        public void accept(Boolean o) {
          if (o.booleanValue()) {
            log.info("reconnect success, register server:{} again.", curServerInfo.toString());
            serverRegistry(curServerInfo);
          }
        }
      });
    } catch (IllegalAccessException e) {
      log.error(e.getMessage());
    }
  }


  /**
   * reconnect success, then reset grpc client and its status
   *
   * @param grpcClient
   */
  public void reconnectSuccessful(GrpcClient grpcClient) {
    log.info("grpc client reconnect success.");
    this.grpcClient = grpcClient;
    this.serviceIsAvaliable = true;
  }


  @Override
  public boolean syncChannelLocationes(TemailAccoutLocations channelLocationes) {
    ChannelLocationes.Builder builder = ChannelLocationes.newBuilder();
    extractGrpcLocations(channelLocationes, builder);
    return this.syncChannelLocationes(builder.build());
  }


  @Override
  public boolean removeChannelLocationes(TemailAccoutLocations channelLocationes) {
    ChannelLocationes.Builder builder = ChannelLocationes.newBuilder();
    extractGrpcLocations(channelLocationes, builder);
    return this.removeChannelLocationes(builder.build());
  }


  private void extractGrpcLocations(TemailAccoutLocations channelLocationes,
      ChannelLocationes.Builder builder) {
    channelLocationes.getStatuses().forEach(lc -> {
      builder.addChannelLocationList(
          ChannelLocation.newBuilder().setAccount(lc.getAccount())
              .setDevId(lc.getDevId()).setHostOf(lc.getHostOf())
              .setProcessId(lc.getProcessId()).setMqTopic(lc.getMqTopic())
              .setMqTag(lc.getMqTag()).build());
    });
  }


  public boolean isServerAvailible() {
    return serviceIsAvaliable;
  }
}
