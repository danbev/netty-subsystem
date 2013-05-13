package org.jboss.aerogear.netty.extension;

import org.jboss.as.controller.AbstractWriteAttributeHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;

class NettyPortHandler extends AbstractWriteAttributeHandler<Void> {

    public static final NettyPortHandler INSTANCE = new NettyPortHandler();

    private NettyPortHandler() {
        super(TypeDefinition.PORT);
    }

    protected boolean applyUpdateToRuntime(final OperationContext context, 
            final ModelNode operation, 
            final String attributeName, 
            final ModelNode resolvedValue, 
            final ModelNode currentValue, 
            final HandbackHolder<Void> handbackHolder) throws OperationFailedException {
        
        if (attributeName.equals(NettyExtension.PORT)) {
            final String serverName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
            final NettyService service = (NettyService) context.getServiceRegistry(true).getRequiredService(NettyService.createServiceName(serverName)).getValue();
            service.setPort(resolvedValue.asInt());
            context.completeStep();
        }
        return false;
    }

    protected void revertUpdateToRuntime(final OperationContext context, 
            final ModelNode operation, 
            final String attributeName, 
            final ModelNode valueToRestore, 
            final ModelNode valueToRevert, 
            final Void handback) {
        // no-op
    }
}
