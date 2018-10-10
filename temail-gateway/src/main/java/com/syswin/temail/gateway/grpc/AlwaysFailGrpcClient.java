package com.syswin.temail.gateway.grpc;

import com.syswin.temail.channel.grpc.servers.ChannelLocations;
import com.syswin.temail.channel.grpc.servers.GatewayServer;

public class AlwaysFailGrpcClient implements GrpcClient {

  private final String host = null;
  private final int port = 0;

  public AlwaysFailGrpcClient() {
  }

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public boolean serverRegistry(GatewayServer gatewayServer) {
    return false;
  }

  @Override
  public boolean serverHeartBeat(GatewayServer gatewayServer) {
    return false;
  }

  @Override
  public boolean syncChannelLocations(ChannelLocations channelLocations) {
    return false;
  }

  @Override
  public boolean removeChannelLocations(ChannelLocations channelLocations) {
    return false;
  }
}
