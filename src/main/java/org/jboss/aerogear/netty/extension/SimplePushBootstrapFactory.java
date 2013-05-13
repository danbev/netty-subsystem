package org.jboss.aerogear.netty.extension;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.jboss.aerogear.netty.extension.api.ServerBootstrapFactory;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.jboss.aerogear.simplepush.server.netty.WebSocketChannelInitializer;

public class SimplePushBootstrapFactory implements ServerBootstrapFactory {

    @Override
    public ServerBootstrap createServerBootstrap() {
        final DataStore datastore = new InMemoryDataStore();
        final WebSocketChannelInitializer channelInitializer = new WebSocketChannelInitializer(datastore, false);
        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        final ServerBootstrap sb = new ServerBootstrap();
        sb.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(channelInitializer);
        return sb;
    }

}
