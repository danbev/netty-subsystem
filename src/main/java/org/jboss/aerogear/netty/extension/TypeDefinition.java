package org.jboss.aerogear.netty.extension;

import static org.jboss.aerogear.netty.extension.NettyExtension.TYPE;
import static org.jboss.aerogear.netty.extension.NettyExtension.TYPE_PATH;

import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

public class TypeDefinition extends SimpleResourceDefinition {
    public static final TypeDefinition INSTANCE = new TypeDefinition();

    protected static final SimpleAttributeDefinition PORT =
            new SimpleAttributeDefinitionBuilder(NettyExtension.PORT, ModelType.INT)
                    .setAllowExpression(true)
                    .setXmlName(NettyExtension.PORT)
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

    private TypeDefinition() {
        super(TYPE_PATH,
                NettyExtension.getResourceDescriptionResolver(TYPE),
                TypeAdd.INSTANCE,
                TypeRemove.INSTANCE);
    }

    @Override
    public void registerAttributes(final ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(PORT, null, NettyPortHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(FACTORY_CLASS, null, NettyPortHandler.INSTANCE);
    }
}
