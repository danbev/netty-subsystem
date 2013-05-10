package org.jboss.aerogear.simplepush.extension;

import static org.jboss.aerogear.simplepush.extension.SimplePushExtension.TYPE;
import static org.jboss.aerogear.simplepush.extension.SimplePushExtension.TYPE_PATH;

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
            new SimpleAttributeDefinitionBuilder(SimplePushExtension.PORT, ModelType.INT)
                    .setAllowExpression(true)
                    .setXmlName(SimplePushExtension.PORT)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(7777))
                    .setAllowNull(false)
                    .build();


    private TypeDefinition() {
        super(TYPE_PATH,
                SimplePushExtension.getResourceDescriptionResolver(TYPE),
                TypeAdd.INSTANCE,
                TypeRemove.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(PORT, null, SimplePushPortHandler.INSTANCE);
    }
}
