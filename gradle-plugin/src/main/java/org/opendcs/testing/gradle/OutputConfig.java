package org.opendcs.testing.gradle;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

/**
 * Define an output location to push data to.
 */
public abstract class OutputConfig
{
    private final String name;

    @javax.inject.Inject
    public OutputConfig(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    /**
     * Type of output
     * @return
     */
    public abstract Property<String> getType();
    /**
     * URL to push data to
     * @return
     */
    public abstract Property<String> getUrl();
    /**
     * Username
     * @return
     */
    public abstract Property<String> getUsername();
    /**
     * Password
     * @return
     */
    public abstract Property<String> getPassword();
    /**
     * Version of product/project. Defaults to project.version
     * @return
     */
    public abstract Property<String> getVersion();
    /**
     * Product name, defaults to project.name
     * @return
     */
    public abstract Property<String> getProduct();
    /**
     * Specific plans to upload.
     * NOTE: Not Yet implemented.
     * @return
     */
    public abstract ListProperty<String> getSelectedPlans();
}
