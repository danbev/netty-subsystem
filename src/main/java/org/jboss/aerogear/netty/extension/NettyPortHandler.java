/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        super(ServerDefinition.PORT);
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
