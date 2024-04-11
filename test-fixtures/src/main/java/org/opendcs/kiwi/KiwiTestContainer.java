package org.opendcs.kiwi;

import java.io.File;

import org.testcontainers.containers.ComposeContainer;

/**
 * Helper to let downstream tests get a KiwiTcms instance going.
 */
public class KiwiTestContainer extends ComposeContainer {
    

    public KiwiTestContainer(File composeFile) {
        super(composeFile);
        withExposedService("web-1", 8443);
    }

    public String getKiwiUrl() {
        return super.getServiceHost("web-1", 8443) 
             + ":"
             + super.getServicePort("web-1", 8443); 
    }
    
}
