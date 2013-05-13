package org.jboss.aerogear.netty.extension;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;

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

    private final ServerBootstrap serverBootstrap;
    private final String name;
    private Channel channel;

    public NettyService(final String name, final int port, final ServerBootstrap serverBootstrap) {
        this.name = name;
        this.port.set(port);
        this.serverBootstrap = serverBootstrap;
    }

    @Override
    public void start(final StartContext context) throws StartException {
        logger.info("NettyService [" + name + "] binding to port [" + port.get() + "]");
        try {
            channel = serverBootstrap.bind(port.get()).sync().channel();
        } catch (InterruptedException e) {
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext context) {
        logger.info("NettyService [" + name + "] shutting down.");
        channel.eventLoop().shutdownGracefully();
    }
    
    @Override
    public NettyService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public static ServiceName createServiceName(final String name) {
        return ServiceName.JBOSS.append("netty", name);
    }

    void setPort(final int port) {
        this.port.set(port);
    }

    public int getPort() {
        return port.get();
    }
}
