package com.syswin.temail.gateway.grpc;

import com.sun.corba.se.spi.presentation.rmi.IDLNameTranslator;
import com.syswin.temail.channel.grpc.servers.ChannelLocation;
import com.syswin.temail.channel.grpc.servers.ChannelLocationes;
import com.syswin.temail.channel.grpc.servers.GatewayServer;
import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.entity.TemailAccoutLocations;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class GrpcSelfReconnectTest {

  public GatewayServer gatewayServer;

  public GrpcClientWrapper grpcClientWrapper;

  public TemailGatewayProperties temailGatewayProperties;

  public List<ChannelLocationes> channelLocationes = new ArrayList<>();

  public List<TemailAccoutLocations> temailAccoutLocations = new ArrayList<>();

  @Before
  public void init4Test() {
    this.temailGatewayProperties = new TemailGatewayProperties();
    this.temailGatewayProperties.setGrpcServerHost("172.31.243.22");
    this.temailGatewayProperties.setGrpcServerPort("9110");
    this.temailGatewayProperties.getInstance().setHostOf("1.1.1.1");
    this.temailGatewayProperties.getInstance().setProcessId("2222222");
    this.grpcClientWrapper = new GrpcClientWrapper(temailGatewayProperties);
  }


  @Test
  public void testReconnect() throws InterruptedException {
    ChannelLocationes channelLocationes = ChannelLocationes.newBuilder()
        .addChannelLocationList(
            ChannelLocation.newBuilder().setAccount("sean@temail").setDevId("devId-1").setHostOf("192.168.197.123")
                .setMqTag("MqTag-1").setMqTopic("Mqtopic-1").setProcessId("232feewwew").build()).addChannelLocationList(
            ChannelLocation.newBuilder().setAccount("sean@temail").setDevId("devId-1").setHostOf("192.168.197.123")
                .setMqTag("MqTag-1").setMqTopic("Mqtopic-1").setProcessId("232feewwew").build()).build();

   for(int i = 0; i < 5; i++){
      try {
        this.grpcClientWrapper.getGrpcClient().removeChannelLocationes(channelLocationes);
      } catch (Exception e) {
        e.printStackTrace();
      }
      TimeUnit.SECONDS.sleep(10);
   }

}
