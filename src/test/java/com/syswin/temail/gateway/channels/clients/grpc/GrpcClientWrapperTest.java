package com.syswin.temail.gateway.channels.clients.grpc;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Ignore;
import org.junit.Test;


@Ignore
@Slf4j
public class GrpcClientWrapperTest {

  private final Random rand = new Random(2);
  private final int gateServes = 8;
  List<GrpcConcurrentData> grpcTestUnits;
  private ExecutorService executorService;

  /**
   * in this method, 8 threads will keep calling grpc servers by calling different methods(removeChannelLocations,
   * asysnChannelLocations, heartBear, etc.) , and in a single thread we will keep disconnect the grpClients of the 8
   * threads.
   * <p>
   * in this condition, the behavior of the grpcClient wo hoped is : 1、at the same time, there will be only one
   * reconnect logic being executed, and other calls will fail fast. 2、when grpcClient reconnect suffessfully, the
   * callers will at once konw this, and executing biz call again . 3、no thread lock competition or thread block will
   * happen even when the grpc client is trying to reconnect server .
   */
  @Test
  public void testConnectionRecovery() throws InterruptedException {
    //mock concurrent request from client
    this.grpcTestUnits = new GrpcConcurentDataUtil(gateServes, null, null).getGrpcTestUnits();
    this.executorService = Executors.newFixedThreadPool(grpcTestUnits.size());
    for (int i = 0; i < grpcTestUnits.size(); i++) {
      GrpcConcurrentData grpcConcurrentData = grpcTestUnits.get(i);
      this.executorService.submit(new GrpcConcurrentTaskOnLine(grpcConcurrentData));
    }
    //keep setting the rpcClient unavailible
    TimeUnit.SECONDS.sleep(3);
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < grpcTestUnits.size(); j++) {
        GrpcConcurrentData grpcConcurrentData = grpcTestUnits.get(j);
        grpcConcurrentData.changeClientStatusUnavailible();
        TimeUnit.MILLISECONDS.sleep(20 + rand.nextInt(20));
      }
      //this will be enough time for client to recovery connection
      TimeUnit.SECONDS.sleep(3);
    }

    //finally we assert that all the client is availible
    for (int i = 0; i < grpcTestUnits.size(); i++) {
      AssertionsForClassTypes.assertThat(grpcTestUnits.get(i)
          .getGrpcClientWrapper().getGrpcClientReference().get() instanceof GrpcClientImpl);
    }
  }
}