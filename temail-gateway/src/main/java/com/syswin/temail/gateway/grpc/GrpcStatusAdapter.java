package com.syswin.temail.gateway.grpc;

import com.syswin.temail.gateway.entity.TemailAccoutLocations;

public interface GrpcStatusAdapter {

  /**
   * add chanel locations
   * @param channelLocationes
   * @return
   */
   boolean syncChannelLocationes(TemailAccoutLocations channelLocationes);

  /**
   * remove channel locations
   * @param channelLocationes
   * @return
   */
   boolean removeChannelLocationes(TemailAccoutLocations channelLocationes);

}
