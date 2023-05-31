package com.stu.demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * ClassName: NettyServer
 * Description:
 *
 * @Author: shenjiaqi
 * 编辑于：2023-05-28 下午5:47   @Version 1.0        描述
 */
public class NettyServer {
    public static void main(String[] args) throws InterruptedException{
        //1.创建bossGroup线程组:处理网络事件--连接事件
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        //2.创建workerGroup线程组:处理网络事件--读写事件 2*处理器线程数
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        //3.创建服务端启动助手
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        //4.设置bossGroup线程组和workerGroup线程组
        serverBootstrap.group(bossGroup,workerGroup)//第一个bossGroup负责连接，
                .channel(NioServerSocketChannel.class)//5.设置服务端通道实现为NIO
                .option(ChannelOption.SO_BACKLOG, 128)//6.参数设置-设置线程队列中等待连接的个数
                .childOption(ChannelOption.SO_KEEPALIVE,Boolean.TRUE)//7.参数设置-设置活跃状态，child是设置workerGroup
                .childHandler(new ChannelInitializer<SocketChannel>() {//8.创建一个通道初始化对象
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //9.向pipeline中添加自定义业务处理handler
                        ch.pipeline().addLast(new NettyServerHandle());
                    }
                });
        //10.启动服务端并绑定端口,同时将异步改为同步
        ChannelFuture future = serverBootstrap.bind(9999).sync();
        //添加监听器
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("端口绑定成功!");
                } else {
                    System.out.println("端口绑定失败！");
                }
            }
        });
        System.out.println("服务器启动成功...");
        //11.关闭通道(并不是真正意义上的关闭，而是监听通道关闭的状态)和关闭连接池
        future.channel().closeFuture().sync();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
