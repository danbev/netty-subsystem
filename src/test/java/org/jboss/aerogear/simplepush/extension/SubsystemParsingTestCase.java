package org.jboss.aerogear.simplepush.extension;


import org.junit.Assert;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;
import org.junit.Test;

import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUCCESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;


/**
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class SubsystemParsingTestCase extends AbstractSubsystemTest {

    public SubsystemParsingTestCase() {
        super(SimplePushExtension.SUBSYSTEM_NAME, new SimplePushExtension());
    }

    /**
     * Tests that the xml is parsed into the correct operations
     */
    @Test
    public void testParseSubsystem() throws Exception {
        //Parse the subsystem xml into operations
        String subsystemXml =
                "<subsystem xmlns=\"" + SimplePushExtension.NAMESPACE + "\">" +
                        "   <simplepush-server>" +
                        "       <deployment-type suffix=\"tst\" port=\"7777\"/>" +
                        "   </simplepush-server>" +
                        "</subsystem>";
        List<ModelNode> operations = super.parse(subsystemXml);

        ///Check that we have the expected number of operations
        Assert.assertEquals(2, operations.size());

        //Check that each operation has the correct content
        //The add subsystem operation will happen first
        ModelNode addSubsystem = operations.get(0);
        Assert.assertEquals(ADD, addSubsystem.get(OP).asString());
        PathAddress addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
        Assert.assertEquals(1, addr.size());
        PathElement element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(SimplePushExtension.SUBSYSTEM_NAME, element.getValue());

        //Then we will get the add type operation
        ModelNode addType = operations.get(1);
        Assert.assertEquals(ADD, addType.get(OP).asString());
        Assert.assertEquals(7777, addType.get("port").asInt());
        addr = PathAddress.pathAddress(addType.get(OP_ADDR));
        Assert.assertEquals(2, addr.size());
        element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(SimplePushExtension.SUBSYSTEM_NAME, element.getValue());
        element = addr.getElement(1);
        Assert.assertEquals("type", element.getKey());
        Assert.assertEquals("tst", element.getValue());
    }

    /**
     * Test that the model created from the xml looks as expected
     */
    @Test
    public void testInstallIntoController() throws Exception {
        final String subsystemXml =
                "<subsystem xmlns=\"" + SimplePushExtension.NAMESPACE + "\">" +
                        "   <simplepush-server>" +
                        "       <deployment-type suffix=\"tst\" port=\"7777\"/>" +
                        "   </simplepush-server>" +
                        "</subsystem>";
        final KernelServices services = super.installInController(subsystemXml);

        final ModelNode model = services.readWholeModel();
        Assert.assertTrue(model.get(SUBSYSTEM).hasDefined(SimplePushExtension.SUBSYSTEM_NAME));
        Assert.assertTrue(model.get(SUBSYSTEM, SimplePushExtension.SUBSYSTEM_NAME).hasDefined("type"));
        Assert.assertTrue(model.get(SUBSYSTEM, SimplePushExtension.SUBSYSTEM_NAME, "type").hasDefined("tst"));
        Assert.assertTrue(model.get(SUBSYSTEM, SimplePushExtension.SUBSYSTEM_NAME, "type", "tst").hasDefined("port"));
        Assert.assertEquals(7777, model.get(SUBSYSTEM, SimplePushExtension.SUBSYSTEM_NAME, "type", "tst", "port").asInt());
    }

    /**
     * Starts a controller with a given subsystem xml and then checks that a second
     * controller started with the xml marshalled from the first one results in the same model
     */
    @Test
    public void testParseAndMarshalModel() throws Exception {
        final String subsystemXml =
                "<subsystem xmlns=\"" + SimplePushExtension.NAMESPACE + "\">" +
                        "   <simplepush-server>" +
                        "       <deployment-type suffix=\"tst\" port=\"7777\"/>" +
                        "   </simplepush-server>" +
                        "</subsystem>";
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

    /**
     * Starts a controller with the given subsystem xml and then checks that a second
     * controller started with the operations from its describe action results in the same model
     */
    @Test
    public void testDescribeHandler() throws Exception {
        final String subsystemXml =
                "<subsystem xmlns=\"" + SimplePushExtension.NAMESPACE + "\">" +
                        "</subsystem>";
        final KernelServices servicesA = super.installInController(subsystemXml);
        
        final ModelNode modelA = servicesA.readWholeModel();
        final ModelNode describeOp = new ModelNode();
        describeOp.get(OP).set(DESCRIBE);
        describeOp.get(OP_ADDR).set(
                PathAddress.pathAddress(
                        PathElement.pathElement(SUBSYSTEM, SimplePushExtension.SUBSYSTEM_NAME)).toModelNode());
        final List<ModelNode> operations = super.checkResultAndGetContents(servicesA.executeOperation(describeOp)).asList();

        //Install the describe options from the first controller into a second controller
        final KernelServices servicesB = super.installInController(operations);
        final ModelNode modelB = servicesB.readWholeModel();

        //Make sure the models from the two controllers are identical
        super.compare(modelA, modelB);

    }

    /**
     * Tests that the subsystem can be removed
     */
    @Test
    public void testSubsystemRemoval() throws Exception {
        final String subsystemXml =
                "<subsystem xmlns=\"" + SimplePushExtension.NAMESPACE + "\">" +
                        "   <simplepush-server>" +
                        "       <deployment-type suffix=\"tst\" port=\"12345\"/>" +
                        "   </simplepush-server>" +
                        "</subsystem>";
        final KernelServices services = super.installInController(subsystemXml);

        //Sanity check to test the service for 'tst' was there
        services.getContainer().getRequiredService(SimplePushService.createServiceName("tst"));

        //Checks that the subsystem was removed from the model
        super.assertRemoveSubsystemResources(services);

        //Check that any services that were installed were removed here
        try {
            services.getContainer().getRequiredService(SimplePushService.createServiceName("tst"));
            Assert.fail("Should have removed services");
        } catch (Exception expected) {
        }
    }

    @Test
    public void testExecuteOperations() throws Exception {
        final String subsystemXml =
                "<subsystem xmlns=\"" + SimplePushExtension.NAMESPACE + "\">" +
                        "   <simplepush-server>" +
                        "       <deployment-type suffix=\"tst\" port=\"12345\"/>" +
                        "   </simplepush-server>" +
                        "</subsystem>";
        final KernelServices services = super.installInController(subsystemXml);

        //Add another type
        final PathAddress fooTypeAddr = PathAddress.pathAddress(
                PathElement.pathElement(SUBSYSTEM, SimplePushExtension.SUBSYSTEM_NAME),
                PathElement.pathElement("type", "foo"));
        final ModelNode addOp = new ModelNode();
        addOp.get(OP).set(ADD);
        addOp.get(OP_ADDR).set(fooTypeAddr.toModelNode());
        addOp.get("port").set(1000);
        ModelNode result = services.executeOperation(addOp);
        Assert.assertEquals(SUCCESS, result.get(OUTCOME).asString());


        final ModelNode model = services.readWholeModel();
        Assert.assertTrue(model.get(SUBSYSTEM).hasDefined(SimplePushExtension.SUBSYSTEM_NAME));
        Assert.assertTrue(model.get(SUBSYSTEM, SimplePushExtension.SUBSYSTEM_NAME).hasDefined("type"));
        Assert.assertTrue(model.get(SUBSYSTEM, SimplePushExtension.SUBSYSTEM_NAME, "type").hasDefined("tst"));
        Assert.assertTrue(model.get(SUBSYSTEM, SimplePushExtension.SUBSYSTEM_NAME, "type", "tst").hasDefined("port"));
        Assert.assertEquals(12345, model.get(SUBSYSTEM, SimplePushExtension.SUBSYSTEM_NAME, "type", "tst", "port").asLong());

        Assert.assertTrue(model.get(SUBSYSTEM, SimplePushExtension.SUBSYSTEM_NAME, "type").hasDefined("foo"));
        Assert.assertTrue(model.get(SUBSYSTEM, SimplePushExtension.SUBSYSTEM_NAME, "type", "foo").hasDefined("port"));
        Assert.assertEquals(1000, model.get(SUBSYSTEM, SimplePushExtension.SUBSYSTEM_NAME, "type", "foo", "port").asLong());

        //Call write-attribute
        final ModelNode writeOp = new ModelNode();
        writeOp.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
        writeOp.get(OP_ADDR).set(fooTypeAddr.toModelNode());
        writeOp.get(NAME).set("port");
        writeOp.get(VALUE).set(3456);
        result = services.executeOperation(writeOp);
        Assert.assertEquals(SUCCESS, result.get(OUTCOME).asString());

        //Check that write attribute took effect, this time by calling read-attribute instead of reading the whole model
        final ModelNode readOp = new ModelNode();
        readOp.get(OP).set(READ_ATTRIBUTE_OPERATION);
        readOp.get(OP_ADDR).set(fooTypeAddr.toModelNode());
        readOp.get(NAME).set("port");
        result = services.executeOperation(readOp);
        Assert.assertEquals(3456, checkResultAndGetContents(result).asLong());

        final SimplePushService service = (SimplePushService) services.getContainer().getService(SimplePushService.createServiceName("foo")).getValue();
        Assert.assertEquals(3456, service.getPort());
    }
}
