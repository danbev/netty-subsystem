# JBoss AS 7.x Subsystem for Netty
This project adds a AS7/Wildfly subsystem for Netty. Currently the features are very limited to our needs in 
AeroGear SimplePush were we simply want to start a Netty server within AS7/Wildfly.

### Prerequisites
This project has a dependency to aerogear-simplepush-server which will be removed later:

    git clone git@github.com:danbev/aerogear-simplepush-server.git
    cd aerogear-simplepush-server
    mvn install

## Configuration
For now, an example configuration in ```AS7_HOME/standalone/configuration/standalone.xml``` could look like this:
    <extensions>
        ...
        <extension module="org.jboss.aerogear.netty"/>
    <extensions>

    <profile>
        ...
        <subsystem xmlns="urn:org.jboss.aerogear.netty:1.0">
            <netty>
                <server name="simplepush-server" socket-binding="simplepush" factoryClass="org.jboss.aerogear.netty.extension.SimplePushBootstrapFactory"/>
                ...
            </netty>
        </subsystem>
    </profile>    
    
     <socket-binding-group name="standard-sockets" default-interface="public" port-offset="${jboss.socket.binding.port-offset:0}">
         ...
         <socket-binding name="simplepush" port="7777"/>
     </socket-binding-group>
    
One or more _server_ elements can be added enabling different types of servers to be run.  

####name  
This is a simple name to identify the server in logs etc.

####socket-binding  
The socket-binding to be used for this Netty server instance. An instance of ```SocketBinding``` will be passed into 
the factory class's ```createServerBootstrap``` method.

####factoryClass  
This is a class that implements ```org.jboss.aerogear.netty.extension.api.ServerBootstrapFactory```:

    public interface ServerBootstrapFactory {
        ServerBootstrap createServerBootstrap(SocketBinding socketBinding);
    }

## Building

    mvn package
    
A JBoss Modules module will be generated in ```target/module/org/jboss/aerogear/netty/main```.    

## Installation
Update ```AS7_HOME/standalone/configuration/standalone.xml``` could look like this:

    <extensions>
        ...
        <extension module="org.jboss.aerogear.netty"/>
    <extensions>
    
    <profile>
        ...
        <subsystem xmlns="urn:org.jboss.aerogear.netty:1.0">
            <netty>
                <server name="simplepush-server" socket-binding="simplepush" factoryClass="org.jboss.aerogear.netty.extension.SimplePushBootstrapFactory"/>
            </netty>
        </subsystem>
    </profile>
    
    <socket-binding-group name="standard-sockets" default-interface="public" port-offset="${jboss.socket.binding.port-offset:0}">
        ...
        <socket-binding name="simplepush" port="7777"/>
    </socket-binding-group>
    
Copy the module produced by ```mvn package``` to the _modules_ directory of the application server.

    cp -r target/module $AS7_HOME/modules
    
    
    