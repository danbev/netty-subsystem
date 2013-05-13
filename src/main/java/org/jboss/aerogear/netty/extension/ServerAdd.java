package org.jboss.aerogear.netty.extension;

import io.netty.bootstrap.ServerBootstrap;

import java.util.List;

import org.jboss.aerogear.netty.extension.api.ServerBootstrapFactory;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;

class ServerAdd extends AbstractAddStepHandler {

    public static final ServerAdd INSTANCE = new ServerAdd();

    private ServerAdd() {
    }
   
    @Override
    protected void populateModel(final ModelNode operation, final ModelNode model) throws OperationFailedException {
        ServerDefinition.PORT.validateAndSet(operation, model);
        ServerDefinition.FACTORY_CLASS.validateAndSet(operation, model);
    }

    @Override
    protected void performRuntime(final OperationContext context, 
            final ModelNode operation, 
            final ModelNode model,
            final ServiceVerificationHandler verificationHandler, 
            final List<ServiceController<?>> newControllers) throws OperationFailedException {
        
        final String factoryClass = ServerDefinition.FACTORY_CLASS.resolveModelAttribute(context, model).asString();
        final ServerBootstrap serverBootstrap = createServerBootstrap(factoryClass);
        final int port = ServerDefinition.PORT.resolveModelAttribute(context, model).asInt();
        final String serverName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
        
        final NettyService service = new NettyService(serverName, port, serverBootstrap);
        
        final ServiceName name = NettyService.createServiceName(serverName);
        final ServiceController<NettyService> controller = context.getServiceTarget()
                .addService(name, service)
                .addListener(verificationHandler)
                .setInitialMode(Mode.ACTIVE)
                .install();
        newControllers.add(controller);
    }
    
    private ServerBootstrap createServerBootstrap(final String factoryClass) throws OperationFailedException {
        try {
            final Class<?> type = Class.forName(factoryClass);
            final ServerBootstrapFactory factory = (ServerBootstrapFactory) type.newInstance();
            return factory.createServerBootstrap();
        } catch (final ClassNotFoundException e) {
            throw new OperationFailedException(e.getMessage());
        } catch (InstantiationException e) {
            throw new OperationFailedException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new OperationFailedException(e.getMessage());
        }
    }
}
