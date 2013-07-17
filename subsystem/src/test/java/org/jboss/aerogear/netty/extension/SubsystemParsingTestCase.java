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

import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.AdditionalInitialization;
import org.jboss.as.subsystem.test.ControllerInitializer;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.as.threads.ThreadFactoryService;
import org.jboss.as.threads.ThreadsServices;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceNotFoundException;
import org.jboss.msc.service.ServiceTarget;
import org.junit.Test;

public class SubsystemParsingTestCase extends AbstractSubsystemTest {
    
    private final String subsystemXml =
        "<subsystem xmlns=\"" + NettyExtension.NAMESPACE + "\">" +
            "<server name=\"simplepush\" socket-binding=\"simplepush\" thread-factory=\"netty-thread-factory\" factoryClass=\"" + MockServerBootstrapFactory.class.getName() + "\"/>" +
        "</subsystem>";

    public SubsystemParsingTestCase() {
        super(NettyExtension.SUBSYSTEM_NAME, new NettyExtension());
    }
    
    @Test
    public void parseAddSubsystem() throws Exception {
        final List<ModelNode> operations = super.parse(subsystemXml);
        System.out.println(operations);
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
        System.out.println(addType);
        assertThat(addType.get(OP).asString(), equalTo(ADD));
        assertThat(addType.get(ServerDefinition.SOCKET_BINDING).asString(), is("simplepush"));
        assertThat(addType.get(ServerDefinition.FACTORY_CLASS).asString(), equalTo(MockServerBootstrapFactory.class.getName()));
        
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
        final KernelServices services = super.installInController(new AdditionalServices(), subsystemXml);

        final ModelNode model = services.readWholeModel();
        assertThat(model.get(SUBSYSTEM).hasDefined(NettyExtension.SUBSYSTEM_NAME), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME).hasDefined("server"), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server").hasDefined("simplepush"), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server", "simplepush").hasDefined("socket-binding"), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server", "simplepush").hasDefined("factoryClass"), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server", "simplepush", "socket-binding").asString(), is("simplepush"));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server", "simplepush", "factoryClass").asString(), is(MockServerBootstrapFactory.class.getName()));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server", "simplepush", "thread-factory").asString(), is("netty-thread-factory"));
    }

    @Test
    public void parseAndMarshalModel() throws Exception {
        final KernelServices servicesA = super.installInController(new AdditionalServices(), subsystemXml);
        final ModelNode modelA = servicesA.readWholeModel();
        final String marshalled = servicesA.getPersistedSubsystemXml();

        final KernelServices servicesB = super.installInController(new AdditionalServices(), marshalled);
        final ModelNode modelB = servicesB.readWholeModel();

        super.compare(modelA, modelB);
    }

    @Test
    public void describeHandler() throws Exception {
        final String subsystemXml =
                "<subsystem xmlns=\"" + NettyExtension.NAMESPACE + "\">" +
                        "</subsystem>";
        final KernelServices servicesA = super.installInController(new AdditionalServices(), subsystemXml);
        
        final ModelNode modelA = servicesA.readWholeModel();
        final ModelNode describeOp = new ModelNode();
        describeOp.get(OP).set(DESCRIBE);
        describeOp.get(OP_ADDR).set(
                PathAddress.pathAddress(
                        PathElement.pathElement(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME)).toModelNode());
        final List<ModelNode> operations = super.checkResultAndGetContents(servicesA.executeOperation(describeOp)).asList();

        final KernelServices servicesB = super.installInController(new AdditionalServices(), operations);
        final ModelNode modelB = servicesB.readWholeModel();
        super.compare(modelA, modelB);

    }

    @Test (expected = ServiceNotFoundException.class)
    public void subsystemRemoval() throws Exception {
        final KernelServices services = super.installInController(new AdditionalServices(), subsystemXml);
        services.getContainer().getRequiredService(NettyService.createServiceName("simplepush"));
        super.assertRemoveSubsystemResources(services);
        services.getContainer().getRequiredService(NettyService.createServiceName("simplepush"));
    }
    
    @Test 
    public void executeOperations() throws Exception {
        final KernelServices services = super.installInController(new AdditionalServices(), subsystemXml);
        final PathAddress serverAddress = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME),
                PathElement.pathElement("server", "foo"));
        final ModelNode addOp = new ModelNode();
        addOp.get(OP).set(ADD);
        addOp.get(OP_ADDR).set(serverAddress.toModelNode());
        addOp.get("socket-binding").set("mysocket");
        addOp.get("factoryClass").set(MockServerBootstrapFactory.class.getName());
        addOp.get("thread-factory").set("netty-thread-factory");
        final ModelNode result = services.executeOperation(addOp);
        assertThat(result.get(OUTCOME).asString(), equalTo(SUCCESS));

        final ModelNode model = services.readWholeModel();
        assertThat(model.get(SUBSYSTEM).hasDefined(NettyExtension.SUBSYSTEM_NAME), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME).hasDefined("server"), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server").hasDefined("simplepush"), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server", "simplepush").hasDefined("socket-binding"), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server", "simplepush", "socket-binding").asString(), is("simplepush"));

        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server").hasDefined("foo"), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server", "foo").hasDefined("socket-binding"), is(true));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server", "foo", "socket-binding").asString(), is("mysocket"));
        assertThat(model.get(SUBSYSTEM, NettyExtension.SUBSYSTEM_NAME, "server", "foo", "thread-factory").asString(), is("netty-thread-factory"));
    }

    private static class AdditionalServices extends AdditionalInitialization {
        
        @Override
        protected void setupController(final ControllerInitializer controllerInitializer) {
            controllerInitializer.setBindAddress("127.0.0.1");
            controllerInitializer.addSocketBinding("mysocket", 8888);
            controllerInitializer.addSocketBinding("simplepush", 7777);
        }
        
        @Override
        protected void addExtraServices(final ServiceTarget serviceTarget) {
            final ThreadFactoryService threadFactoryService = new ThreadFactoryService();
            threadFactoryService.setNamePattern("%i");
            threadFactoryService.setPriority(Thread.NORM_PRIORITY);
            threadFactoryService.setThreadGroupName("netty-thread-group");
            final ServiceBuilder<?> serviceBuilder = serviceTarget.addService(ThreadsServices.threadFactoryName("netty-thread-factory"), threadFactoryService);
            serviceBuilder.install();
        }
    }
}
