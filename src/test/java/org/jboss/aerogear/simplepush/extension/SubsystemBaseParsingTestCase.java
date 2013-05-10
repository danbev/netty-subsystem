package org.jboss.aerogear.simplepush.extension;


import org.jboss.aerogear.simplepush.extension.SimplePushExtension;
import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;

import java.io.IOException;


/**
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class SubsystemBaseParsingTestCase extends AbstractSubsystemBaseTest {

    public SubsystemBaseParsingTestCase() {
        super(SimplePushExtension.SUBSYSTEM_NAME, new SimplePushExtension());
    }

    @Override
    protected String getSubsystemXml() throws IOException {
        return readResource("subsystem.xml");
    }
}
