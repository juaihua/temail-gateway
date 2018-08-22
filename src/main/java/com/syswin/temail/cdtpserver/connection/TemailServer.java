package com.syswin.temail.cdtpserver.connection;

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
import lombok.extern.slf4j.Slf4j;

import org.apache.logging.log4j.core.config.Order;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.syswin.temail.cdtpserver.codec.PacketDecoder;
import com.syswin.temail.cdtpserver.codec.PacketEncoder;
import com.syswin.temail.cdtpserver.entity.TemailMqInfo;
import com.syswin.temail.cdtpserver.handler.TemailServerHandler;
import com.syswin.temail.cdtpserver.handler.factory.HandlerFactory;
import com.syswin.temail.cdtpserver.properties.TemailServerProperties;
import com.syswin.temail.cdtpserver.status.TemailSocketSyncClient;
import com.syswin.temail.cdtpserver.utils.LocalMachineUtil;
import com.syswin.temail.cdtpserver.utils.TemailMqInfBuilder;

/**
 * Created by weis on 18/8/2.
 */
@Component
@Order(1)
@Slf4j
public class TemailServer implements ApplicationRunner {

    @Resource
    HandlerFactory   handlerFactory;
    
    @Resource
    TemailServerProperties   temailServerProperties;
    
    @Resource
    TemailSocketSyncClient temailSocketSyncClient;
    
    @Override
    public void run(ApplicationArguments args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();// 默认 cup

        ServerBootstrap b = new ServerBootstrap();

        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                // 指定使用NIO传输Channel
                .localAddress(new InetSocketAddress(temailServerProperties.getPort()))
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
                                                
                        pipeline.addLast("idleStateHandler",new IdleStateHandler(0,0,temailServerProperties.getAllIdleTimeSeconds()));
                                              
                        TemailServerHandler temailServerHandler = new TemailServerHandler();                      
                        setTemailServerHandlerProperties(temailServerHandler);
                        pipeline.addLast(temailServerHandler);
                    }
                    
                                        
                    private  void   setTemailServerHandlerProperties(TemailServerHandler temailServerHandler){
                      TemailMqInfo  temailMqInfo = TemailMqInfBuilder.getTemailMqInf(temailServerProperties);
                      handlerFactory.setTemailMqInfo(temailMqInfo);                      
                      temailServerHandler.setHandlerFactory(handlerFactory);                       
                      temailServerHandler.setTemailSocketSyncClient(temailSocketSyncClient);
                      temailServerHandler.setTemailMqInfo(temailMqInfo);
                      temailServerHandler.setTemailServerProperties(temailServerProperties);
                    }
                    
                });


        try {
            // 异步地绑定服务器;调用sync方法阻塞等待直到绑定完成
            ChannelFuture f = b.bind().sync();            
            log.info("Temail 服务器已启动,正在监听用户的请求......");
            // 获取Channel的CloseFuture，并且阻塞当前线程直到它完成
            f.channel().closeFuture().sync();
        } catch (InterruptedException ex) {
            log.error("Temail 服务器已启动", ex);
        } finally {
            // 优雅的关闭EventLoopGroup，释放所有的资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
    
    
    
    
    
}
