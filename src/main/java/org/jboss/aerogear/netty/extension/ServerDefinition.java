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
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

public class ServerDefinition extends SimpleResourceDefinition {
    public static final ServerDefinition INSTANCE = new ServerDefinition();

    protected static final SimpleAttributeDefinition SOCKET_BINDING = new SimpleAttributeDefinition(NettyExtension.SOCKET_BINDING, ModelType.STRING, false);
    protected static final SimpleAttributeDefinition FACTORY_CLASS = new SimpleAttributeDefinition(NettyExtension.FACTORY_CLASS, ModelType.STRING, false);
    /*

    protected static final SimpleAttributeDefinition SOCKET_BINDING =
            new SimpleAttributeDefinitionBuilder(NettyExtension.SOCKET_BINDING, ModelType.STRING)
                    .setAllowExpression(true)
                    .setXmlName(NettyExtension.SOCKET_BINDING)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(7777))
                    .setAllowNull(false)
                    .build();
    
    protected static final SimpleAttributeDefinition FACTORY_CLASS =
            new SimpleAttributeDefinitionBuilder(NettyExtension.FACTORY_CLASS, ModelType.STRING)
                    .setAllowExpression(false)
                    .setXmlName(NettyExtension.FACTORY_CLASS)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setAllowNull(false)
                    .build();
    */

    private ServerDefinition() {
        super(SERVER_PATH,
                NettyExtension.getResourceDescriptionResolver(SERVER),
                ServerAdd.INSTANCE,
                ServerRemove.INSTANCE);
    }

    @Override
    public void registerAttributes(final ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(SOCKET_BINDING, null, NettyPortHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(FACTORY_CLASS, null, NettyPortHandler.INSTANCE);
    }
}
