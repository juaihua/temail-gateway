package com.syswin.temail.cdtpserver.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;

import javax.annotation.Resource;

import lombok.Setter;

import org.apache.logging.log4j.core.config.Order;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.syswin.temail.cdtpserver.handler.HandlerFactory;
import com.syswin.temail.cdtpserver.network.codec.PacketDecoder;
import com.syswin.temail.cdtpserver.network.codec.PacketEncoder;

/**
 * Created by weis on 18/8/2.
 */
@Component
@Order(1)
public class TemailServer implements ApplicationRunner {

    @Setter
    private int port = 8099;

    @Resource
    HandlerFactory   handlerFactory;
    
    
    @Override
    public void run(ApplicationArguments args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();// 默认 cup

        ServerBootstrap b = new ServerBootstrap();

        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                // 指定使用NIO传输Channel
                .localAddress(new InetSocketAddress(port))
                // 通过NoDelay禁用Nagle,使消息立即发送出去
                // .option(ChannelOption.TCP_NODELAY,true)
                // 保持长连接状态
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                // 使用指定端口设置套接字地址
                .childHandler(new ChannelInitializer<SocketChannel>(){

                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        ChannelPipeline pipeline = socketChannel.pipeline();


                        pipeline.addLast(new PacketDecoder());
                        pipeline.addLast(new PacketEncoder());
                                                
                        //pipeline.addLast("idleStateHandler",new IdleStateHandler(0,0,10));
                        pipeline.addLast("idleStateHandler",new IdleStateHandler(0,0,180));
                        
                        TemailServerHandler temailServerHandler = new TemailServerHandler();
                        temailServerHandler.setHandlerFactory(handlerFactory);
                        pipeline.addLast(temailServerHandler);
                    }
                });


        try {
            // 异步地绑定服务器;调用sync方法阻塞等待直到绑定完成
            ChannelFuture f = b.bind().sync();
            System.out.println("服务器已启动,正在监听用户的请求......");
            // 获取Channel的CloseFuture，并且阻塞当前线程直到它完成
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 优雅的关闭EventLoopGroup，释放所有的资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
