package com.syswin.temail.gateway.channels;

import com.syswin.temail.gateway.entity.TemailAccoutLocations;

/**
 * channels sync interface
 */
public interface ChannelsSyncClient {

  /**
   * init client
   */
  void initClient();

  /**
   * destroy client
   */
  void destroyClient();

  /**
   * add chanel locations
   */
  boolean syncChannelLocations(TemailAccoutLocations channelLocations);

  /**
   * remove channel locations
   */
  boolean removeChannelLocations(TemailAccoutLocations channelLocations);

}
