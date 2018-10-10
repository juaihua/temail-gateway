package com.syswin.temail.gateway.grpc;

import com.syswin.temail.channel.grpc.servers.ChannelLocation;
import com.syswin.temail.channel.grpc.servers.ChannelLocations;
import com.syswin.temail.channel.grpc.servers.GatewayServer;
import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.entity.TemailAccoutLocations;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * in wrapper we add reconnect and heartBeat logic and keep original client clean.<p>
 * be aware of keeping client fail fast when channel-server is not available
 * , and my be we can update the channel in batch for this fail fast afterwards ...
 */
@Slf4j
@Data
//@Component
public class GrpcClientWrapper implements GrpcClient, StatusSyncClient {

  private final AtomicReference<GrpcClient> grpcClientReference;
  private final GrpcClient alwaysFailGrpcClient = new AlwaysFailGrpcClient();
  private final TemailGatewayProperties temailGatewayProperties;

  private volatile boolean serviceIsAvaliable = false;

  private final GrpcReconnectManager grpcReconnectManager;

  private final GrpcHeartBeatManager grpcHeartBeatManager;

  private final GrpcClient grpcClient;

  private final GatewayServer curServerInfo;

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
    this.grpcClientReference = new AtomicReference<>(grpcClient);
  }

  // TODO: 2018/10/10 where is corresponding disconnect?
  /**
   * register and start heartBeat for default current server
   */
  public void initClient() {
    this.serverRegistry(curServerInfo);
    grpcHeartBeatManager.heartBeat(new Consumer<Boolean>() {
      @Override
      public void accept(Boolean aBoolean) {
        if (!aBoolean.booleanValue()) {
          // if heartBean fail, that means grpcServer is unavailable
          // and if the serviceIsAvaliable is true means need to reconnect!
          if (serviceIsAvaliable) {
            reconnect(() -> {
            });
          } else {
            log.info("grpc server not available, reconnect is being executed. wait.");
          }
        }
      }
    });
  }


  @Override
  public boolean serverRegistry(GatewayServer gatewayServer) {
    try {
      if (!serviceIsAvaliable) {
        log.warn("server registry fail, cause grpcServer is unavailable, reconnect logic is being executed.");
        return false;
      }
      return grpcClient.serverRegistry(gatewayServer);
    } catch (Exception e) {
      log.error("server registry fail, try to reconnect grpcServer.", e);
      reconnect(() -> {
      });
      return false;
    }
  }


  @Override
  public boolean serverHeartBeat(GatewayServer gatewayServer) {
    try {
      return grpcClientReference.get().serverHeartBeat(gatewayServer);
    } catch (Exception e) {
      if (grpcClientReference.compareAndSet(grpcClient, alwaysFailGrpcClient)) {
        reconnect(() -> grpcClientReference.compareAndSet(alwaysFailGrpcClient, grpcClient));
      }
      return false;
    }
/*
    try {
      if (!serviceIsAvaliable) {
        log.warn("server heart beat fail, cause grpcServer is unavailable, reconnect logic is being executed.");
        return false;
      }
      return grpcClient.serverHeartBeat(gatewayServer);
    } catch (Exception e) {
      log.error("server heart beat fail, try to reconnect grpcServer.", e);
      reconnect();
      return false;
    }
*/
  }


  @Override
  public boolean syncChannelLocations(ChannelLocations channelLocations) {
    try {
      if (!serviceIsAvaliable) {
        log.warn("sync channel Locations fail, cause grpcServer is unavailable, reconnect logic is being executed.");
        return false;
      }
      log.info("sync channel Locations success : {}", channelLocations.toString());
      return grpcClient.syncChannelLocations(channelLocations);
    } catch (Exception e) {
      log.error("sync channel Locations fail : {} ", channelLocations.toString(), e);
      reconnect(() -> {
      });
      return false;
    }
  }


  @Override
  public boolean removeChannelLocations(ChannelLocations channelLocations) {
    try {
      if (!serviceIsAvaliable) {
        log.warn(
            "remove channel Locations fail, cause grpcServer is unavailable, reconnect logic is being executed.");
        return false;
      }
      log.info("remove channel Locations success : {} - success. ", channelLocations.toString());
      return grpcClient.removeChannelLocations(channelLocations);
    } catch (Exception e) {
      log.error("remove channel Locations fail : {} ", channelLocations.toString(), e);
      reconnect(() -> {
      });
      return false;
    }
  }


  /**
   * reconnect client, once success then registry the server info again
   * @param runnable
   */
  void reconnect(Runnable runnable) {
    try {
      log.info("grpc client is unavailable, try to reconnect again!");
      serviceIsAvaliable = false;
      grpcReconnectManager.reconnect(t -> { }, runnable);
    } catch (IllegalAccessException e) {
      log.error(e.getMessage());
    }
  }


  /**
   * reconnect success, then reset grpc client and its status
   */
  public void reconnectSuccessful() {
    log.info("grpc client reconnect success.");
    this.serviceIsAvaliable = true;
  }


  @Override
  public boolean syncChannelLocations(TemailAccoutLocations channelLocations) {
    ChannelLocations.Builder builder = ChannelLocations.newBuilder();
    extractGrpcLocations(channelLocations, builder);
    return this.syncChannelLocations(builder.build());
  }


  @Override
  public boolean removeChannelLocations(TemailAccoutLocations channelLocations) {
    ChannelLocations.Builder builder = ChannelLocations.newBuilder();
    extractGrpcLocations(channelLocations, builder);
    return this.removeChannelLocations(builder.build());
  }


  private void extractGrpcLocations(TemailAccoutLocations channelLocations,
      ChannelLocations.Builder builder) {
    channelLocations.getStatuses().forEach(lc -> {
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
