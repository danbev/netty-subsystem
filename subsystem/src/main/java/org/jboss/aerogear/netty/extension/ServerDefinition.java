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

import static org.jboss.aerogear.netty.extension.NettyExtension.SERVER;
import static org.jboss.aerogear.netty.extension.NettyExtension.SERVER_PATH;

import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelType;

public class ServerDefinition extends SimpleResourceDefinition {
    
    protected static final String SOCKET_BINDING = "socket-binding";
    protected static final String THREAD_FACTORY = "thread-factory";
    protected static final String FACTORY_CLASS = "factoryClass";
    protected static final String SERVER_NAME = "name";
    
    protected static final SimpleAttributeDefinition SOCKET_BINDING_ATTR = new SimpleAttributeDefinition(SOCKET_BINDING, ModelType.STRING, false);
    protected static final SimpleAttributeDefinition FACTORY_CLASS_ATTR = new SimpleAttributeDefinition(FACTORY_CLASS, ModelType.STRING, false);
    protected static final SimpleAttributeDefinition THREAD_FACTORY_ATTR = new SimpleAttributeDefinition(THREAD_FACTORY, ModelType.STRING, true);
    
    public static final ServerDefinition INSTANCE = new ServerDefinition();

    private ServerDefinition() {
        super(SERVER_PATH,
                NettyExtension.getResourceDescriptionResolver(SERVER),
                ServerAdd.INSTANCE,
                ServerRemove.INSTANCE);
    }

    @Override
    public void registerAttributes(final ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(SOCKET_BINDING_ATTR, null, NettySocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(FACTORY_CLASS_ATTR, null, NettySocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(THREAD_FACTORY_ATTR, null, NettySocketBindingHandler.INSTANCE);
    }
}
