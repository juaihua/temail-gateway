package com.syswin.temail.gateway.channels;

import com.syswin.temail.gateway.entity.TemailAccoutLocations;

/**
 * channels sync interface
 */
public interface ChannelsSyncClient {

   /**
   * init client
   */
  public void initClient();

  /**
   * destroy client
   */
  public void destroyClient();

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
