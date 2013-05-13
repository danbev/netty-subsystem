package org.jboss.aerogear.netty.extension;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

public class NettyService implements Service<NettyService> {

    private final AtomicInteger port = new AtomicInteger(0);
    private final Logger logger = Logger.getLogger(NettyService.class);

    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final ChannelInitializer<?> channelInitializer;
    private final String name;
    private Channel channel;

    private Thread OUTPUT = new Thread() {
        @Override
        public void run() {
            try {
                final ServerBootstrap sb = new ServerBootstrap();
                sb.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(channelInitializer);
                    
                logger.info("NettyService [" + name + "] binding to port [" + port.get() + "]");
                channel = sb.bind(port.get()).sync().channel();
            } catch (final InterruptedException e) {
                logger.info("Going to disconnect from port [" + port.get() + "]");
                final ChannelFuture disconnect = channel.disconnect();
                try {
                    disconnect.await(1000);
                } catch (final InterruptedException e1) {
                    logger.error(e1);
                } finally {
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                    interrupted();
                }
            }
        }
    };

    public NettyService(final String name, final int port, final ChannelInitializer<?> channelInitializer) {
        this.name = name;
        this.port.set(port);
        this.channelInitializer = channelInitializer;
    }

    @Override
    public NettyService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
        OUTPUT.start();
    }

    @Override
    public void stop(StopContext context) {
        OUTPUT.interrupt();
    }

    public static ServiceName createServiceName(final String suffix) {
        return ServiceName.JBOSS.append("netty", suffix);
    }

    void setPort(final int port) {
        this.port.set(port);
    }

    public int getPort() {
        return port.get();
    }
}
