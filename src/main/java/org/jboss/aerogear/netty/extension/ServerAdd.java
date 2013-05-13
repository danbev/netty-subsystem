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

import java.util.List;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.network.SocketBinding;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;

class ServerAdd extends AbstractAddStepHandler {

    public static final ServerAdd INSTANCE = new ServerAdd();

    private ServerAdd() {
    }
   
    @Override
    protected void populateModel(final ModelNode operation, final ModelNode model) throws OperationFailedException {
        ServerDefinition.SOCKET_BINDING.validateAndSet(operation, model);
        ServerDefinition.FACTORY_CLASS.validateAndSet(operation, model);
    }

    @Override
    protected void performRuntime(final OperationContext context, 
            final ModelNode operation, 
            final ModelNode model,
            final ServiceVerificationHandler verificationHandler, 
            final List<ServiceController<?>> newControllers) throws OperationFailedException {
        final String factoryClass = ServerDefinition.FACTORY_CLASS.resolveModelAttribute(context, model).asString();
        final String socketBinding = ServerDefinition.SOCKET_BINDING.resolveModelAttribute(context, model).asString();
        final ServiceName socketName = SocketBinding.JBOSS_BINDING_NAME.append(socketBinding);
        
        final String serverName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
        final NettyService service = new NettyService(serverName, factoryClass);
        
        final ServiceName name = NettyService.createServiceName(serverName);
        final ServiceBuilder<NettyService> sb = context.getServiceTarget().addService(name, service);
        sb.addDependency(socketName, SocketBinding.class, service.getInjectedSocketBinding());
        sb.addListener(verificationHandler).setInitialMode(Mode.ACTIVE);
        final ServiceController<NettyService> controller = sb.install();
        newControllers.add(controller);
    }
    
}
