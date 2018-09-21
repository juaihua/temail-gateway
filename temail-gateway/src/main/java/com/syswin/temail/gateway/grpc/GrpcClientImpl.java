package com.syswin.temail.gateway.grpc;

import com.syswin.temail.channel.grpc.servers.ChannelLocationes;
import com.syswin.temail.channel.grpc.servers.GatewayRegistrySyncServerGrpc;
import com.syswin.temail.channel.grpc.servers.GatewayServer;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Data;

@Data
class GrpcClientImpl implements GrpcClient {

  private GatewayRegistrySyncServerGrpc.GatewayRegistrySyncServerBlockingStub serverBlockingStub;

  private ManagedChannel channel;

  private String host;

  private int port;

  public GrpcClientImpl() {}

  public GrpcClientImpl(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    this.host = host;
    this.port = port;
  }

  public GrpcClientImpl(ManagedChannelBuilder<?> channelBuilder) {
    this.channel = channelBuilder.build();
    this.serverBlockingStub = GatewayRegistrySyncServerGrpc.newBlockingStub(channel);
  }

  @Override
  public boolean serverRegistry(GatewayServer gatewayServer) {
    return serverBlockingStub.serverRegistry(gatewayServer).getIsSuccess();
  }

  @Override
  public boolean serverHeartBeat(GatewayServer gatewayServer) {
    return serverBlockingStub.serverHeartBeat(gatewayServer).getIsSuccess();
  }

  @Override
  public boolean syncChannelLocationes(ChannelLocationes channelLocationes) {
    return serverBlockingStub.syncChannelLocationes(channelLocationes).getIsSuccess();
  }

  @Override
  public boolean removeChannelLocationes(ChannelLocationes channelLocationes) {
    return serverBlockingStub.removeChannelLocationes(channelLocationes).getIsSuccess();
  }

}
