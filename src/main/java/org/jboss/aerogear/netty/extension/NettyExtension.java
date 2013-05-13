package org.jboss.aerogear.netty.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;


/**
 * Netty Server subsystem for AS 7.x
 */
public class NettyExtension implements Extension {

    /**
     * The name space used for the/ {@code subsystem} element
     */
    public static final String NAMESPACE = "urn:org.jboss.aerogear.netty:1.0";

    /**
     * The name of our subsystem within the model.
     */
    public static final String SUBSYSTEM_NAME = "netty";

    /**
     * The parser used for parsing our subsystem
     */
    private final SubsystemParser parser = new SubsystemParser();

    private static final String RESOURCE_NAME = NettyExtension.class.getPackage().getName() + ".LocalDescriptions";

    protected static final String TYPE = "type";
    protected static final String PORT = "port";
    protected static final String FACTORY_CLASS = "factoryClass";
    protected static final PathElement SUBSYSTEM_PATH = PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME);
    protected static final PathElement TYPE_PATH = PathElement.pathElement(TYPE);

    static StandardResourceDescriptionResolver getResourceDescriptionResolver(final String keyPrefix) {
        String prefix = SUBSYSTEM_NAME + (keyPrefix == null ? "" : "." + keyPrefix);
        return new StandardResourceDescriptionResolver(prefix, RESOURCE_NAME, NettyExtension.class.getClassLoader(), true, false);
    }

    @Override
    public void initializeParsers(final ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, NAMESPACE, parser);
    }

    @Override
    public void initialize(final ExtensionContext context) {
        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, 1, 0);
        final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(NettySubsystemDefinition.INSTANCE);
        registration.registerSubModel(ServerDefinition.INSTANCE);
        subsystem.registerXMLElementWriter(parser);
    }

    /**
     * The subsystem parser, which uses stax to read and write to and from xml
     */
    private static class SubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void readElement(final XMLExtendedStreamReader reader, final List<ModelNode> list) throws XMLStreamException {
            // Require no attributes
            ParseUtils.requireNoAttributes(reader);

            //Add the main subsystem 'add' operation
            final ModelNode subsystem = new ModelNode();
            subsystem.get(OP).set(ADD);
            subsystem.get(OP_ADDR).set(PathAddress.pathAddress(SUBSYSTEM_PATH).toModelNode());
            list.add(subsystem);

            //Read the children
            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                if (!reader.getLocalName().equals("netty")) {
                    throw ParseUtils.unexpectedElement(reader);
                }
                while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                    if (reader.isStartElement()) {
                        readDeploymentType(reader, list);
                    }
                }
            }
        }

        private void readDeploymentType(final XMLExtendedStreamReader reader, final List<ModelNode> list) throws XMLStreamException {
            final ModelNode addTypeOperation = new ModelNode();
            addTypeOperation.get(OP).set(ModelDescriptionConstants.ADD);

            String name = null;
            for (int i = 0; i < reader.getAttributeCount(); i++) {
                final String attr = reader.getAttributeLocalName(i);
                final String value = reader.getAttributeValue(i);
                if (attr.equals("port")) {
                    ServerDefinition.PORT.parseAndSetParameter(value, addTypeOperation, reader);
                } else if (attr.equals("name")) {
                    name = value;
                }  else if (attr.equals(FACTORY_CLASS)) {
                    ServerDefinition.FACTORY_CLASS.parseAndSetParameter(value, addTypeOperation, reader);
                } else {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
            ParseUtils.requireNoContent(reader);
            if (name == null) {
                throw ParseUtils.missingRequiredElement(reader, Collections.singleton("name"));
            }

            //Add the 'add' operation for each 'type' child
            final PathAddress addr = PathAddress.pathAddress(SUBSYSTEM_PATH, PathElement.pathElement(TYPE, name));
            addTypeOperation.get(OP_ADDR).set(addr.toModelNode());
            list.add(addTypeOperation);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {
            //Write out the main subsystem element
            context.startSubsystemElement(NettyExtension.NAMESPACE, false);
            writer.writeStartElement(SUBSYSTEM_NAME);
            final ModelNode node = context.getModelNode();
            final ModelNode type = node.get(TYPE);
            for (Property property : type.asPropertyList()) {
                writer.writeStartElement("server");
                writer.writeAttribute("name", property.getName());
                final ModelNode entry = property.getValue();
                ServerDefinition.PORT.marshallAsAttribute(entry, true, writer);
                ServerDefinition.FACTORY_CLASS.marshallAsAttribute(entry, true, writer);
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }


}
