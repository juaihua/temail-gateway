package com.syswin.temail.gateway.grpc;

import com.syswin.temail.channel.grpc.servers.ChannelLocationes;
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
   * @param channelLocationes
   * @return
   */
  public boolean syncChannelLocationes(ChannelLocationes channelLocationes);

  /**
   * remove channel locations
   * @param channelLocationes
   * @return
   */
  public boolean removeChannelLocationes(ChannelLocationes channelLocationes);

}
