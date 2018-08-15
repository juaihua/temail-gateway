package com.syswin.temail.cdtpserver.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;
import com.syswin.temail.cdtpserver.network.codec.PacketDecoder;
import com.syswin.temail.cdtpserver.network.codec.PacketEncoder;

/**
 * Created by weis on 18/8/3.
 */
public class EchoClient {
    private final static String HOST = "192.168.15.186";
    //private final static String HOST = "127.0.0.1";
    private final static int PORT = 8099;

    public static void start() {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,1024)
                .remoteAddress(new InetSocketAddress(HOST, PORT))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        /*socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                        socketChannel.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
                        socketChannel.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));*/
                /*      socketChannel.pipeline().addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
                      socketChannel.pipeline().addLast("decoder", new  ProtobufDecoder(CDTPPackageProto.CDTPPackage.getDefaultInstance()));
                      socketChannel.pipeline().addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
                      socketChannel.pipeline().addLast("encoder", new ProtobufEncoder());*/
                      
                      socketChannel.pipeline().addLast(new PacketDecoder());
                      socketChannel.pipeline().addLast(new PacketEncoder());
                      
                      //socketChannel.pipeline().addLast(new EchoClientHandler());
                      socketChannel.pipeline().addLast(new EchoClientProtoHandler());
                    }
                });
        try {
//            ChannelFuture f = bootstrap.connect().sync();
//            f.channel().closeFuture().sync();
            Channel channel = bootstrap.connect().sync().channel();
            // 发送json字符串
            String msg = "{\"command\":110,\"from\":\"weisheng@temail.com\",\"to\":\"gaojianhui@temail.com\",\"version\":\"1.0.0\"}\n";
//            String jsonString = "{name:'Antony',age:'22',sex:'male',telephone:'88888'}";
            channel.writeAndFlush(msg);
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        start();
    }

}
