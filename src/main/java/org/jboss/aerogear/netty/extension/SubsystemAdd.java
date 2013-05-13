package org.jboss.aerogear.netty.extension;

import java.util.List;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;

class SubsystemAdd extends AbstractAddStepHandler {

    static final SubsystemAdd INSTANCE = new SubsystemAdd();

    private SubsystemAdd() {
    }

    /** {@inheritDoc} */
    @Override
    protected void populateModel(final ModelNode operation, final ModelNode model) throws OperationFailedException {
    }

    /** {@inheritDoc} */
    @Override
    public void performRuntime(final OperationContext context, 
            final ModelNode operation, 
            final ModelNode model,
            final ServiceVerificationHandler verificationHandler, 
            final List<ServiceController<?>> newControllers)
            throws OperationFailedException {
    }
    
}
