package com.syswin.temail.gateway.grpc;

import com.syswin.temail.channel.grpc.servers.ChannelLocations;
import com.syswin.temail.channel.grpc.servers.GatewayServer;

public interface GrpcClient {

  /**
   * return host of current
   * @return
   */
  public String getHost();

  /**
   * return port of current client
   * @return
   */
  public int getPort();

  /**
   * server registry
   * @param gatewayServer
   * @return
   */
  public boolean serverRegistry(GatewayServer gatewayServer);

  /**
   * server heartBeat
   * @param gatewayServer
   * @return
   */
  public boolean serverHeartBeat(GatewayServer gatewayServer);

  /**
   * add chanel locations
   * @param channelLocations
   * @return
   */
  public boolean syncChannelLocations(ChannelLocations channelLocations);

  /**
   * remove channel locations
   * @param channelLocations
   * @return
   */
  public boolean removeChannelLocations(ChannelLocations channelLocations);

}
