package org.jboss.aerogear.netty.extension;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

import org.jboss.aerogear.netty.extension.api.ChannelInitializerFactory;

public class MockChannelInitFactory implements ChannelInitializerFactory {

    @SuppressWarnings("rawtypes")
    @Override
    public ChannelInitializer<?> createChannelInitializer() {
        return new ChannelInitializer() {
            @Override
            protected void initChannel(final Channel ch) throws Exception {
            }
        };
    }

}
