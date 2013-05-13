package org.jboss.aerogear.netty.extension;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;

class ServerRemove extends AbstractRemoveStepHandler{

    public static final ServerRemove INSTANCE = new ServerRemove();

    private ServerRemove() {
    }

    @Override
    protected void performRuntime(final OperationContext context, 
            final ModelNode operation, 
            final ModelNode model) throws OperationFailedException {
        final String serverName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
        final ServiceName name = NettyService.createServiceName(serverName);
        context.removeService(name);
    }

}
