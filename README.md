# JBoss AS 7.x Subsystem for Netty
This project adds a AS7/Wildfly subsystem for Netty. Currently the features are very limited to our needs in 
AeroGear SimplePush were we simply want to start a Netty server within AS7/Wildfly.

## Configuration
The configuration of the subsystem if very limited at the moment, but we plan to hook this into the normal AS7/Wildfly
configuration using ```socket-binding``` etc. 
For now an example configuration could look like this:

    <subsystem xmlns="urn:org.jboss.aerogear.netty:1.0">
        <netty>
            <server name="simplepush" port="7777" factoryClass="org.jboss.aerogear.netty.extension.SimplePushBootstrapFactory"/>
            ...
        </netty>
    </subsystem>
One or more _server_ elements can be added.  

####name  
This is a simple name to identify the server in logs etc.

####port  
The port that Netty will listen to. This will be changed to be a _socket-binding_.

####factoryClass  
This is a class that implements ```org.jboss.aerogear.netty.extension.api.ServerBootstrapFactory```:

    public interface ServerBootstrapFactory {
        ServerBootstrap createServerBootstrap();
    }

## Building

    mvn package
    
A JBoss Modules module will be generated in ```target/module/org/jboss/aerogear/netty/main```.    

## Installation
Update standalone.xml :

    <extension module="org.jboss.aerogear.netty"/>
    ...
    <subsystem xmlns="urn:org.jboss.aerogear.netty:1.0">
        <netty>
            <server name="simplepush" port="7777" factoryClass="org.jboss.aerogear.netty.extension.SimplePushBootstrapFactory"/>
        </netty>
    </subsystem>
    
Copy the module produced by ```mvn package``` to the _modules_ directory of the application server.

    cp -r target/module $AS7_HOME/modules
    
    
    