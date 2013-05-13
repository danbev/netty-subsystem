package org.jboss.aerogear.netty.extension;

import io.netty.channel.ChannelInitializer;

import org.jboss.aerogear.netty.extension.api.ChannelInitializerFactory;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.jboss.aerogear.simplepush.server.netty.WebSocketChannelInitializer;

public class SimplePushChannelInitializer implements ChannelInitializerFactory {

    @Override
    public ChannelInitializer<?> createChannelInitializer() {
        final DataStore datastore = new InMemoryDataStore();
        return new WebSocketChannelInitializer(datastore, false);
    }

}
