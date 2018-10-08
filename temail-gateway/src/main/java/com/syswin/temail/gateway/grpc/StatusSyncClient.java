package com.syswin.temail.gateway.grpc;

import com.syswin.temail.gateway.entity.TemailAccoutLocations;

public interface StatusSyncClient {

   /**
   * init client
   */
   public void initClient();

  /**
   * add chanel locations
   * @param channelLocations
   * @return
   */
   boolean syncChannelLocations(TemailAccoutLocations channelLocations);

  /**
   * remove channel locations
   * @param channelLocations
   * @return
   */
   boolean removeChannelLocations(TemailAccoutLocations channelLocations);

}
