package com.syswin.temail.gateway.channels.clients.grpc;

import com.syswin.temail.channel.grpc.servers.ChannelLocations;
import com.syswin.temail.channel.grpc.servers.GatewayServer;

public interface GrpcClient {

  /**
   * disconnect grpc connections
   */
  public void closeConnection();

  /**
   * detect connection state between grpc client and grpc server
   */
  public boolean retryConnection(GatewayServer gatewayServer);

  /**
   * server registry
   *
   * @param gatewayServer
   * @return
   */
  public boolean serverRegistry(GatewayServer gatewayServer);

  /**
   * server offLine
   *
   * @param gatewayServer
   * @return
   */
  public boolean serverOffLine(GatewayServer gatewayServer);

  /**
   * server heartBeat
   *
   * @param gatewayServer
   * @return
   */
  public boolean serverHeartBeat(GatewayServer gatewayServer);

  /**
   * add chanel locations
   *
   * @param channelLocations
   * @return
   */
  public boolean syncChannelLocations(ChannelLocations channelLocations);

  /**
   * remove channel locations
   *
   * @param channelLocations
   * @return
   */
  public boolean removeChannelLocations(ChannelLocations channelLocations);

}
