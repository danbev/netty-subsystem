package org.jboss.aerogear.netty.extension.api;

import io.netty.channel.ChannelInitializer;

public interface ChannelInitializerFactory {
    
    ChannelInitializer<?> createChannelInitializer();

}
