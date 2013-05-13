package org.jboss.aerogear.netty.extension;


import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;

import java.io.IOException;


public class SubsystemBaseParsingTestCase extends AbstractSubsystemBaseTest {

    public SubsystemBaseParsingTestCase() {
        super(NettyExtension.SUBSYSTEM_NAME, new NettyExtension());
    }

    @Override
    protected String getSubsystemXml() throws IOException {
        return readResource("subsystem.xml");
    }
}
