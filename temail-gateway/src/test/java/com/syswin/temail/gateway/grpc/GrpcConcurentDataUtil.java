package com.syswin.temail.gateway.grpc;

import com.syswin.temail.channel.grpc.servers.ChannelLocation;
import com.syswin.temail.channel.grpc.servers.ChannelLocationes;
import com.syswin.temail.channel.grpc.servers.GatewayServer;
import com.syswin.temail.gateway.entity.TemailAccoutLocation;
import com.syswin.temail.gateway.entity.TemailAccoutLocations;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcConcurentDataUtil {

  private Random random = new Random(2);

  private Integer gateServerCount;

  private Integer lcCountPerServer;

  private Integer lcCountPerLocationes;

  @Getter
  public List<GrpcConcurrentData> grpcTestUnits;

  public GrpcConcurentDataUtil(Integer gateServerCount,
      Integer lcCountPerServer, Integer lcCountPerLocationes) {
    this.gateServerCount = Optional.ofNullable(gateServerCount).orElse(10);
    this.lcCountPerServer = Optional.ofNullable(lcCountPerServer).orElse(200);
    this.lcCountPerLocationes = Optional.ofNullable(lcCountPerLocationes).orElse(1+random.nextInt(4));
    this.grpcTestUnits = this.geneData();
  }

  public List<GrpcConcurrentData> geneData(){
    grpcTestUnits = new ArrayList<>();
    for (Integer i = 0; i < gateServerCount; i++) {
      GrpcConcurrentData grpcTestUnit = new GrpcConcurrentData();
      GatewayServer gatewayServer = GatewayServer.newBuilder()
          .setIp(CommonDataGeneUtil.extractIp())
          .setProcessId(CommonDataGeneUtil.extractUUID())
          .build();

      for (Integer j = 0; j < lcCountPerServer; j++) {
        ChannelLocationes.Builder channelLocationesBuilder = ChannelLocationes.newBuilder();

        for (Integer k = 0; k < lcCountPerLocationes; k++) {
          channelLocationesBuilder.addChannelLocationList(ChannelLocation.newBuilder()
              .setDevId(CommonDataGeneUtil.extractChar(CommonDataGeneUtil.ExtractType.UPPER, 10))
              .setAccount(CommonDataGeneUtil.extractChar(CommonDataGeneUtil.ExtractType.LOWER, 5) + "@temail")
              .setHostOf(gatewayServer.getIp())
              .setProcessId(gatewayServer.getProcessId())
              .setMqTag("maTag-" + CommonDataGeneUtil.extractChar(CommonDataGeneUtil.ExtractType.LOWER, 5))
              .setMqTopic("maTopic-" + CommonDataGeneUtil.extractChar(CommonDataGeneUtil.ExtractType.LOWER, 5))
              .setProcessId(CommonDataGeneUtil.extractUUID())
              .build());
        }
        ChannelLocationes channelLocationes = channelLocationesBuilder.build();
        grpcTestUnit.getChannelLocationes().add(channelLocationes);
        grpcTestUnit.getTemailAccoutLocations().add(transfrom(channelLocationes));
      }
      grpcTestUnit.setGatewayServer(gatewayServer);
      grpcTestUnits.add(grpcTestUnit);
    }
    return grpcTestUnits;
  }
  
  public TemailAccoutLocations transfrom(ChannelLocationes channelLocationes){
    List<TemailAccoutLocation> accoutLocations = new ArrayList<TemailAccoutLocation>();
    TemailAccoutLocations temailAccoutLocations = new TemailAccoutLocations(accoutLocations);
    for (ChannelLocation channelLocation : channelLocationes.getChannelLocationListList()) {
      TemailAccoutLocation temailAccoutLocation = new TemailAccoutLocation();
      temailAccoutLocation.setAccount(channelLocation.getAccount());
      temailAccoutLocation.setDevId(channelLocation.getDevId());
      temailAccoutLocation.setHostOf(channelLocation.getHostOf());
      temailAccoutLocation.setMqTag(channelLocation.getMqTag());
      temailAccoutLocation.setMqTopic(channelLocation.getMqTopic());
      temailAccoutLocation.setProcessId(channelLocation.getProcessId());
    }
    return temailAccoutLocations;
  }

}
