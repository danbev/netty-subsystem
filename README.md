# WildFly Subsystem for Netty
This project adds a Wildfly subsystem for Netty. The reason for creating this was that we wanted to be able to 
deploy the [AeroGear SimplePush Server](https://github.com/danbev/aerogear-simplepush-server), which uses Netty, on WildFly. We
also wanted something that was reusable as we might later on want to migrate the [Unified Push Server](https://github.com/matzew/pushee)
to Netty.

The goal is to enable users to configure one or more Netty server applications that are managed by WildFly.  

__This is a work in progress and not complete__.

## Prerequisites
This project has a dependency to aerogear-simplepush-server which will be removed later:

    git clone git@github.com:danbev/aerogear-simplepush-server.git
    cd aerogear-simplepush-server
    mvn install
    
## Building

    mvn package
    
A JBoss Modules module will be generated in _target/module/org/jboss/aerogear/netty/main_.    

## Installation
Copy the module produced by ```mvn package``` to the _modules_ directory of the application server.

    cp -r target/module $WILDFLY_HOME/modules

## Usage

### Adding the subsystem to WildFly
The Netty subsystem can be added to any of the configurations that are shipped with WildFly. 
As an example, add the following elements to _$WILDFLYHOME/standalone/configuration/standalone.xml_.


#### Add the extension

    <extensions>
        ...
        <extension module="org.jboss.aerogear.netty"/>
    <extensions>
    
    
#### Add a socket-binding    

    
    <socket-binding-group name="standard-sockets" default-interface="public" port-offset="${jboss.socket.binding.port-offset:0}">
        ...
        <socket-binding name="simplepush" port="7777"/>
    </socket-binding-group>  

#### Add the Netty subsystem

    <profile>
        ...
        <subsystem xmlns="urn:org.jboss.aerogear.netty:1.0">
            <netty>
                <server name="simplepush-server" socket-binding="simplepush" factoryClass="org.jboss.aerogear.netty.extension.SimplePushBootstrapFactory"/>
                ...
            </netty>
        </subsystem>
    </profile>    
    
One or more _server_ elements can be added enabling different types of servers to be run.  

__name__  
This is a simple name to identify the server in logs etc.

__socket-binding__  
The socket-binding to be used for this Netty server instance. An instance of _SocketBinding_ will be passed into 
the factory class's _createServerBootstrap_ method.

__factoryClass__  
This is a class that implements _org.jboss.aerogear.netty.extension.api.ServerBootstrapFactory_ and is responsible for 
creating a [ServerBootstrap](http://netty.io/4.0/api/io/netty/bootstrap/ServerBootstrap.html). This allows the end user to
configure the Netty server application with the appropriate _Channel_, _ChannelPipeline_ etc.  
The sole method, _createServerBootstrap_, takes a single parameter which is a [SocketBinding](https://github.com/wildfly/wildfly/blob/master/network/src/main/java/org/jboss/as/network/SocketBinding.java) instance:

    public interface ServerBootstrapFactory {
        ServerBootstrap createServerBootstrap(SocketBinding socketBinding);
    }
    
The _ServerBoostrapFactory_ interface is currently part of this project but should be extracted to a separate dependency so it would 
be the only dependency that a project wanting to integrate with WildFly would have to implement.



    
    
    