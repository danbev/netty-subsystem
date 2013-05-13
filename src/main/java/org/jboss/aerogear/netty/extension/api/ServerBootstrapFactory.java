package org.jboss.aerogear.netty.extension.api;

import io.netty.bootstrap.ServerBootstrap;

public interface ServerBootstrapFactory {
    
    ServerBootstrap createServerBootstrap();

}
