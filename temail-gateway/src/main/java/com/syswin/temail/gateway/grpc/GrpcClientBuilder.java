package com.syswin.temail.gateway.grpc;

public class GrpcClientBuilder {

  private final String host;

  private final int port;

  public GrpcClientBuilder(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public GrpcClient buildGrpcClient() {
    return new GrpcClientImpl(host, port);
  }
}
