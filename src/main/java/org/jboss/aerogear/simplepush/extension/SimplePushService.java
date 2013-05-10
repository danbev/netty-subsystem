package org.jboss.aerogear.simplepush.extension;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.jboss.aerogear.simplepush.server.netty.WebSocketChannelInitializer;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

public class SimplePushService implements Service<SimplePushService> {

    private AtomicInteger port = new AtomicInteger(7777);

    //private Set<String> deployments = Collections.synchronizedSet(new HashSet<String>());
    //private Set<String> coolDeployments = Collections.synchronizedSet(new HashSet<String>());
    //private final String suffix;
    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private static Channel channel;

    private Thread OUTPUT = new Thread() {
        @Override
        public void run() {
            while (true) {
                try {
                    final DataStore datastore = new InMemoryDataStore();
                    final ServerBootstrap sb = new ServerBootstrap();
                    sb.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new WebSocketChannelInitializer(datastore, false));
                    
                    System.out.println("Going to bind to port [" + port.get() + "]");
                        
                    channel = sb.bind(port.get()).sync().channel();
                    //Thread.sleep(port.get());
                    //System.out.println("Current deployments deployed while " + suffix + " tracking active:\n" + deployments + "\nCool: " + coolDeployments.size());
                } catch (final InterruptedException e) {
                    System.out.println("Going to disconnect from port [" + port.get() + "]");
                    final ChannelFuture disconnect = channel.disconnect();
                    try {
                        disconnect.await(1000);
                    } catch (final InterruptedException e1) {
                        e1.printStackTrace();
                    } finally {
                        bossGroup.shutdownGracefully();
                        workerGroup.shutdownGracefully();
                        interrupted();
                    }
                    break;
                }
            }
        }
    };

    public SimplePushService(final String suffix, final int port) {
        //this.suffix = suffix;
        this.port.set(port);
    }

    @Override
    public SimplePushService getValue() throws IllegalStateException, IllegalArgumentException {
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

    public static ServiceName createServiceName(String suffix) {
        return ServiceName.JBOSS.append("simplepush", suffix);
    }

    /*
    public void addDeployment(String name) {
        deployments.add(name);
    }

    public void addCoolDeployment(String name) {
        coolDeployments.add(name);
    }

    public void removeDeployment(String name) {
        deployments.remove(name);
        coolDeployments.remove(name);
    }
    */

    void setPort(final int port) {
        this.port.set(port);
    }

    public int getPort() {
        return port.get();
    }
}
