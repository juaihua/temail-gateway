package com.syswin.temail.gateway.channels.clients.grpc;

import com.syswin.temail.channel.grpc.servers.ChannelLocation;
import com.syswin.temail.channel.grpc.servers.ChannelLocations;
import com.syswin.temail.channel.grpc.servers.GatewayServer;
import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.channels.ChannelsSyncClient;
import com.syswin.temail.gateway.entity.TemailAccoutLocations;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * in wrapper we add reconnect and heartBeat logic to keep original client clean.<p>
 * be aware of keeping client fail fast when channel-server is not available and may
 * be we can update the channels in batch for this fail fast afterwards ...
 */
@Slf4j
@Data
//@Component
public class GrpcClientWrapper implements GrpcClient, ChannelsSyncClient {

  private final GrpcClient alwaysFailGrpcClient = new AlwaysFailGrpcClient();

  private final AtomicReference<GrpcClient> grpcClientReference;

  private final TemailGatewayProperties temailGatewayProperties;

  private final GrpcHeartBeatManager grpcHeartBeatManager;

  private final GrpcReconnectManager grpcReconnectManager;

  private final GatewayServer curServerInfo;

  private final GrpcClient grpcClient;

  @Autowired
  public GrpcClientWrapper(TemailGatewayProperties temailGatewayProperties) {
    this.temailGatewayProperties = temailGatewayProperties;
    this.grpcReconnectManager = new GrpcReconnectManager(this, temailGatewayProperties);
    this.grpcHeartBeatManager = new GrpcHeartBeatManager(this, temailGatewayProperties);
    this.grpcClient = new GrpcClientImpl(temailGatewayProperties.getGrpcServerHost(),
        Integer.valueOf(temailGatewayProperties.getGrpcServerPort()));
    this.grpcClientReference = new AtomicReference<>(grpcClient);
    this.curServerInfo = GatewayServer.newBuilder()
        .setProcessId(temailGatewayProperties.getInstance().getProcessId())
        .setIp(temailGatewayProperties.getInstance().getHostOf())
        .build();
  }


  @Override
  public void initClient() {
    this.serverRegistry(curServerInfo);
    grpcHeartBeatManager.heartBeat();
  }


  @Override
  public void destroyClient() {
    this.serverOffLine(curServerInfo);
    this.closeConnection();
  }

  @Override
  public boolean retryConnection(GatewayServer gatewayServer) {
    try {
      return grpcClientReference.get().retryConnection(gatewayServer);
    } catch (Exception e) {
      log.error("try connect with grpc server fail.", e);
      return false;
    }
  }

  @Override
  public void closeConnection() {
    this.grpcClient.closeConnection();
  }


  @Override
  public boolean serverRegistry(GatewayServer gatewayServer) {
    try {
      return grpcClientReference.get().serverRegistry(gatewayServer);
    } catch (Exception e) {
      log.error("server registry fail, try to reconnect grpcServer.", e);
      reconnect();
      return false;
    }
  }


  @Override
  public boolean serverOffLine(GatewayServer gatewayServer) {
    try {
      return grpcClientReference.get().serverOffLine(gatewayServer);
    } catch (Exception e) {
      //even fail, the server offLine will be executed by channel server
      //when heart beat timeout so do not try to reconnect again.
      log.error("server offLine fail, try to reconnect grpcServer.", e);
      return false;
    }
  }


  @Override
  public boolean serverHeartBeat(GatewayServer gatewayServer) {
    try {
      return grpcClientReference.get().serverHeartBeat(gatewayServer);
    } catch (Exception e) {
      reconnect();
      return false;
    }
  }


  @Override
  public boolean syncChannelLocations(ChannelLocations channelLocations) {
    try {
      log.info("sync channel Locations success : {}", channelLocations.toString());
      return grpcClientReference.get().syncChannelLocations(channelLocations);
    } catch (Exception e) {
      log.error("sync channel Locations fail : {} ", channelLocations.toString(), e);
      reconnect();
      return false;
    }
  }


  @Override
  public boolean removeChannelLocations(ChannelLocations channelLocations) {
    try {
      log.info("remove channel Locations success : {} - success. ", channelLocations.toString());
      return grpcClientReference.get().removeChannelLocations(channelLocations);
    } catch (Exception e) {
      log.error("remove channel Locations fail : {} ", channelLocations.toString(), e);
      reconnect();
      return false;
    }
  }


  /**
   * reconnect client by trying to registry current server.
   */
  void reconnect() {
    if (grpcClientReference.compareAndSet(grpcClient, alwaysFailGrpcClient)) {
      log.info("grpc client is unavailable, try to reconnect!");
      grpcReconnectManager.reconnect(
          () -> grpcClientReference.compareAndSet(alwaysFailGrpcClient, grpcClient));
    }
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
    channelLocations.getStatuses().forEach(lc ->
        builder.addChannelLocationList(
            ChannelLocation.newBuilder().setAccount(lc.getAccount())
                .setDevId(lc.getDevId()).setHostOf(lc.getHostOf())
                .setProcessId(lc.getProcessId()).setMqTopic(lc.getMqTopic())
                .setMqTag(lc.getMqTag()).build()));
  }
}
