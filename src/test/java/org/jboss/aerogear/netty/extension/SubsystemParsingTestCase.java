package org.jboss.aerogear.netty.extension;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUCCESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;

import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceNotFoundException;
import org.junit.Test;

public class SubsystemParsingTestCase extends AbstractSubsystemTest {
    
    private final String subsystemXml =
                "<subsystem xmlns=\"" + NettyExtension.NAMESPACE + "\">" +
                        "   <netty>" +
                        "       <server name=\"simplepush\" port=\"7777\" factoryClass=\"" + MockChannelInitFactory.class.getName() + "\"/>" +
                        "   </netty>" +
                        "</subsystem>";

    public SubsystemParsingTestCase() {
        super(NettyExtension.SUBSYSTEM_NAME, new NettyExtension());
    }
    
    @Test
    public void parseAddSubsystem() throws Exception {
        final List<ModelNode> operations = super.parse(subsystemXml);
        assertThat(operations.size(), is(2));

        final ModelNode addSubsystem = operations.get(0);
        assertThat(addSubsystem.get(OP).asString(), equalTo(ADD));
        final PathAddress addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
        assertThat(addr.size(), is(1));
        final PathElement element = addr.getElement(0);
        assertThat(element.getKey(), equalTo(SUBSYSTEM));
        assertThat(element.getValue(), equalTo(NettyExtension.SUBSYSTEM_NAME));
    }

    @Test
    public void parseAddType() throws Exception {
        final List<ModelNode> operations = super.parse(subsystemXml);
        assertThat(operations.size(), is(2));
        final ModelNode addType = operations.get(1);
        assertThat(addType.get(OP).asString(), equalTo(ADD));
        assertThat(addType.get(NettyExtension.PORT).asInt(), is(7777));
        assertThat(addType.get(NettyExtension.FACTORY_CLASS).asString(), equalTo(MockChannelInitFactory.class.getName()));
        
        final PathAddress addr = PathAddress.pathAddress(addType.get(OP_ADDR));
        assertThat(addr.size(), is(2));
        final PathElement firstPathElement = addr.getElement(0);
        assertThat(firstPathElement.getKey(), equalTo(SUBSYSTEM));
        assertThat(firstPathElement.getValue(), equalTo(NettyExtension.SUBSYSTEM_NAME));
        final PathElement secondPathElement = addr.getElement(1);
        assertThat(secondPathElement.getKey(), equalTo("server"));
        assertThat(secondPathElement.getValue(), equalTo("simplepush"));
    }

    @Test
    public void installIntoController() throws Exception {
        final KernelServices services = super.installInController(subsystemXml);

        final ModelNode model = services.readWholeModel();
        assertThat(model.get(SUBSYSTEM).hasDefined(NettyExtension.SUBSYSTEM_NAME), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME).hasDefined("server"), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server").hasDefined("simplepush"), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server", "simplepush").hasDefined("port"), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server", "simplepush").hasDefined("factoryClass"), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server", "simplepush", "port").asInt(), is(7777));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server", "simplepush", "factoryClass").asString(), is(MockChannelInitFactory.class.getName()));
    }

    @Test
    public void parseAndMarshalModel() throws Exception {
        final KernelServices servicesA = super.installInController(subsystemXml);
        //Get the model and the persisted xml from the first controller
        final ModelNode modelA = servicesA.readWholeModel();
        final String marshalled = servicesA.getPersistedSubsystemXml();

        //Install the persisted xml from the first controller into a second controller
        final KernelServices servicesB = super.installInController(marshalled);
        final ModelNode modelB = servicesB.readWholeModel();

        //Make sure the models from the two controllers are identical
        super.compare(modelA, modelB);
    }

    @Test
    public void describeHandler() throws Exception {
        final String subsystemXml =
                "<subsystem xmlns=\"" + NettyExtension.NAMESPACE + "\">" +
                        "</subsystem>";
        final KernelServices servicesA = super.installInController(subsystemXml);
        
        final ModelNode modelA = servicesA.readWholeModel();
        final ModelNode describeOp = new ModelNode();
        describeOp.get(OP).set(DESCRIBE);
        describeOp.get(OP_ADDR).set(
                PathAddress.pathAddress(
                        PathElement.pathElement(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME)).toModelNode());
        final List<ModelNode> operations = super.checkResultAndGetContents(servicesA.executeOperation(describeOp)).asList();

        //Install the describe options from the first controller into a second controller
        final KernelServices servicesB = super.installInController(operations);
        final ModelNode modelB = servicesB.readWholeModel();

        //Make sure the models from the two controllers are identical
        super.compare(modelA, modelB);

    }

    @Test (expected = ServiceNotFoundException.class)
    public void subsystemRemoval() throws Exception {
        final KernelServices services = super.installInController(subsystemXml);
        services.getContainer().getRequiredService(NettyService.createServiceName("simplepush"));
        super.assertRemoveSubsystemResources(services);
        services.getContainer().getRequiredService(NettyService.createServiceName("simplepush"));
    }

    @Test 
    public void executeOperations() throws Exception {
        final KernelServices services = super.installInController(subsystemXml);

        //Add another type
        final PathAddress fooTypeAddr = PathAddress.pathAddress(
                PathElement.pathElement(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME),
                PathElement.pathElement("server", "foo"));
        final ModelNode addOp = new ModelNode();
        addOp.get(OP).set(ADD);
        addOp.get(OP_ADDR).set(fooTypeAddr.toModelNode());
        addOp.get("port").set(1000);
        addOp.get("factoryClass").set(MockChannelInitFactory.class.getName());
        final ModelNode result = services.executeOperation(addOp);
        assertThat(result.get(OUTCOME).asString(), equalTo(SUCCESS));


        final ModelNode model = services.readWholeModel();
        assertThat(model.get(SUBSYSTEM).hasDefined(NettyExtension.SUBSYSTEM_NAME), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME).hasDefined("server"), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server").hasDefined("simplepush"), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server", "simplepush").hasDefined("port"), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server", "simplepush", "port").asInt(), is(7777));

        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server").hasDefined("foo"), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server", "foo").hasDefined("port"), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server", "foo", "port").asInt(), is(1000));

        //Call write-attribute
        final ModelNode writeOp = new ModelNode();
        writeOp.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
        writeOp.get(OP_ADDR).set(fooTypeAddr.toModelNode());
        writeOp.get(NAME).set("port");
        writeOp.get(VALUE).set(3456);
        final ModelNode result2 = services.executeOperation(writeOp);
        assertThat(result2.get(OUTCOME).asString(), is(SUCCESS));

        //Check that write attribute took effect, this time by calling read-attribute instead of reading the whole model
        final ModelNode readOp = new ModelNode();
        readOp.get(OP).set(READ_ATTRIBUTE_OPERATION);
        readOp.get(OP_ADDR).set(fooTypeAddr.toModelNode());
        readOp.get(NAME).set("port");
        final ModelNode result3 = services.executeOperation(readOp);
        assertThat(checkResultAndGetContents(result3).asLong(), is(3456L));

        final NettyService service = (NettyService) services.getContainer().getService(NettyService.createServiceName("foo")).getValue();
        assertThat(service.getPort(), is(3456));
    }
}