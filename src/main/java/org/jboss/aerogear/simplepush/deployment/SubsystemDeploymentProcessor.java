package org.jboss.aerogear.simplepush.deployment;

import org.jboss.aerogear.simplepush.extension.SimplePushService;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.vfs.VirtualFile;

public class SubsystemDeploymentProcessor implements DeploymentUnitProcessor {

    Logger log = Logger.getLogger(SubsystemDeploymentProcessor.class);

    /**
     * See {@link Phase} for a description of the different phases
     */
    public static final Phase PHASE = Phase.DEPENDENCIES;

    /**
     * The relative order of this processor within the {@link #PHASE}.
     * The current number is large enough for it to happen after all
     * the standard deployment unit processors that come with JBoss AS.
     */
    public static final int PRIORITY = 0x4000;

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final String name = phaseContext.getDeploymentUnit().getName();
        final ResourceRoot root = phaseContext.getDeploymentUnit().getAttachment(Attachments.DEPLOYMENT_ROOT);
        final SimplePushService service = getSimplePushService(phaseContext.getServiceRegistry(), name);
        if (service != null) {
            final VirtualFile cool = root.getRoot().getChild("META-INF/cool.txt");
            service.addDeployment(name);
            if (cool.exists()) {
                service.addCoolDeployment(name);
            }
        }
    }

    @Override
    public void undeploy(final DeploymentUnit context) {
        context.getServiceRegistry();
        final String name = context.getName();
        final SimplePushService service = getSimplePushService(context.getServiceRegistry(), name);
        if (service != null) {
            service.removeDeployment(name);
        }
    }

    private SimplePushService getSimplePushService(final ServiceRegistry registry, final String name) {
        final int last = name.lastIndexOf(".");
        final String suffix = name.substring(last + 1);
        final ServiceController<?> container = registry.getService(SimplePushService.createServiceName(suffix));
        if (container != null) {
            final SimplePushService service = (SimplePushService)container.getValue();
            return service;
        }
        return null;
    }
}
