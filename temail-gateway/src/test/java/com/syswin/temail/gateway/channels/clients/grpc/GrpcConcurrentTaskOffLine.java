package com.syswin.temail.gateway.channels.clients.grpc;

import java.util.Random;
import java.util.function.Consumer;
import lombok.Data;

/**
 * 1、test connectoin recovery
 * 2、generate trash data to clean
 */
@Data
public class GrpcConcurrentTaskOffLine implements Runnable {

  private static final Random RANDOM = new Random(2);

  private final int cyclicTimes = 5;

  private final GrpcConcurrentData grpcConcrData;

  private final Consumer<Object> consumer;

  public GrpcConcurrentTaskOffLine(GrpcConcurrentData GrpcConcurrentData, Consumer<Object> consumer){
    this.grpcConcrData = GrpcConcurrentData;
    this.consumer = consumer;
  }

  @Override
  public void run() {
    //start registry and heartBeat
    grpcConcrData.init4Test();
    grpcConcrData.grpcClientWrapper.initClient();
    //begin the test
    int curIndex = 0;
    for (int i = 0; i < grpcConcrData.temailAccoutLocations.size(); i++) {
      grpcConcrData.grpcClientWrapper.syncChannelLocations(grpcConcrData.temailAccoutLocations.get(i));
    }
    if( consumer != null){
      this.consumer.accept("finsished");
    }
  }
}
