package org.jboss.aerogear.netty.extension;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.jboss.aerogear.netty.extension.api.ServerBootstrapFactory;

public class MockServerBootstrapFactory implements ServerBootstrapFactory {

    @Override
    public ServerBootstrap createServerBootstrap() {
        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        final ServerBootstrap sb = new ServerBootstrap();
        sb.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
            }
        });
        return sb;
    }

}
