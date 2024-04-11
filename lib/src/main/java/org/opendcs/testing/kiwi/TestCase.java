package org.opendcs.testing.kiwi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class TestCase {
    private final long id;
    private final String summary;
    private final String steps;
    private final Priority priority;
    private final Product product;
    private final Category category;
    private final String requirements;
    private final String notes;
    private final String arguments;
    private final String referenceLink;
    private final String status;
    private final List<Attachment> attachments;
    private final List<String> tags;
    private final List<Component> components;
    private final Properties properties;

    private TestCase(long id, String summary, String steps, Priority priority,
                     Product product, Category category, String requirements,
                     String notes, String arguments, String referenceLink,
                     String status, List<Attachment> attachments, List<String> tags,
                     List<Component> components, Properties properties) {
        this.id = id;
        this.summary = summary;
        this.steps = steps;
        this.priority = priority;
        this.product = product;
        this.category = category;
        this.requirements = requirements;
        this.notes = notes;
        this.arguments = arguments;
        this.referenceLink = referenceLink;
        this.status = status;
        this.attachments = attachments;
        this.tags = tags;
        this.components = components;
        this.properties = properties;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public long getId() {
        return id;
    }

    public String getSummary() {
        return summary;
    }

    public String getSteps() {
        return steps;
    }

    public Priority getPriority() {
        return priority;
    }

    public Product getProduct() {
        return product;
    }

    public Category getCategory() {
        return category;
    }

    public String getRequirements() {
        return requirements;
    }

    public String getNotes() {
        return notes;
    }

    public String getArguments() {
        return arguments;
    }

    public String getReferenceLink() {
        return referenceLink;
    }

    public String getStatus() {
        return status;
    }

    public List<Attachment> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }

    public List<String> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public List<Component> getComponents() {
        return Collections.unmodifiableList(components);
    }

    /**
     *
     * @return A copy of the properties object.
     */
    public Properties getProperties() {
        return new Properties(properties);
    }

    public String getProperty(String value) {
        return properties.getProperty(value);
    }

    @Override
    public String toString() {
        return "testcase{"
             + "Summary=" + this.summary + ","
             + "Product=" + this.product.name + ","
             + "Category=" + this.category.name + ","
             + "Components=" + this.components.toString()
             + "}";
    }

    public static class Builder {
        private long id = -1;
        private List<Attachment> attachments = new ArrayList<>();
        private List<String> tags = new ArrayList<>();
        private List<Component> components = new ArrayList<>();
        private String summary = null;
        private String steps = null;
        private Priority priority = null;
        private Product product = null;
        private Category category = null;
        private String requirements = null;
        private String notes = null;
        private String arguments = null;
        private String referenceLink = null;
        private String status = "CONFIRMED";
        private Properties properties = new Properties();

        /**
         * Start a new builder for a given product, using category '--default--' by default.
         * @param productName
         */
        public Builder(String productName) {
            this.product = Product.of(productName);
            this.category = Category.of(product, "--default--");
        }

        /**
         * New builder from an existing test case.
         * @param tc
         */
        public Builder(TestCase tc) {
            this.id = tc.id;
            this.summary = tc.summary;
            this.steps = tc.steps;
            this.priority = tc.priority;
            this.product = tc.product;
            this.category = tc.category;
            this.requirements = tc.requirements;
            this.notes = tc.notes;
            this.arguments = tc.arguments;
            this.referenceLink = tc.referenceLink;
            this.status = tc. status;
            this.attachments = new ArrayList<>(attachments);
            this.tags = new ArrayList<>(tags);
            this.components = new ArrayList<>(components);
            this.properties.putAll(tc.properties);
        }

        public TestCase build() {
            return new TestCase(id, summary, steps, priority, product,
                                category, requirements,notes,arguments,referenceLink,status,
                                attachments, tags, components, properties);
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        /**
         * Add a component with the given name, will assume the current product.
         * @param component
         * @return
         */
        public Builder withComponent(String component)
        {
            this.components.add(Component.of(product, component));
            return this;
        }

        /**
         * Set the summary/title of the test case.
         * @param summary
         * @return
         */
        public Builder withSummary(String summary)
        {
            this.summary = summary;
            return this;
        }

        /**
         * This is the general text of the test itself.
         * @param steps
         * @return
         */
        public Builder withSteps(String steps)
        {
            this.steps = steps;
            return this;
        }

        /**
         * Arbitrary tag information that may be used for searching or other
         * organization in Kiwi.
         * @param tag
         * @return
         */
        public Builder withTag(String tag)
        {
            this.tags.add(tag);
            return this;
        }

        /**
         * Additional notes to the tester for a given test case.
         * @param notes
         * @return
         */
        public Builder withNotes(String notes)
        {
            this.notes = notes;
            return this;
        }

        /**
         * Short string describing a specific requirement for the test.
         * @param requirements
         * @return
         */
        public Builder withRequirements(String requirements)
        {
            this.requirements = requirements;
            return this;
        }

        /**
         * Set the test category, with new product name.
         * NOTE: this will alter the product of the testcase. and may invalidate components.
         * @param category
         * @param productName
         * @return
         */
        public Builder withCategory(String category, String productName)
        {
            this.product = Product.of(productName);
            this.category = Category.of(product, category);
            return this;
        }

        /**
         * Set the test category, using the established product.
         * @param category
         * @return
         */
        public Builder withCategory(String category)
        {
            this.category = Category.of(this.product, category);
            return this;
        }

        /**
         * Set the test priority. Valid values depend on your Kiwi instance.
         * @param priority
         * @return
         */
        public Builder withPriority(String priority)
        {
            this.priority = Priority.of(priority);
            return this;
        }

        /**
         * Set the test status. Valid values depend on your Kiwi instance.
         * @param status
         * @return
         */
        public Builder withStatus(String status)
        {
            this.status = status;
            return this;
        }

        /**
         * A URL to additional resources for the test. Or the test itself.
         * @param link
         * @return
         */
        public Builder withReferenceLink(String link)
        {
            this.referenceLink = link;
            return this;
        }

        /**
         * Reset the entire list of components.
         * @param components
         * @return
         */
        public Builder withComponents(List<Component> components) {
            this.components.clear();
            this.components.addAll(components);
            return this;
        }

        /**
         * Property to set or unset
         * @param name name of the property
         * @param value value for the property. If null property will be removed.
         * @return
         */
        public Builder withProperty(String name, String value)
        {
            if (value != null)
            {
                this.properties.setProperty(name, value);
            }
            else
            {
                this.properties.remove(name);
            }
            return this;
        }

        public long getId() {
            return id;
        }
    }

    /**
     * Wrapper class to hold property values.
     */
    public static class TestCaseProperty
    {
        public final long id;
        public final long caseId;
        public final String name;
        public final String value;

        public TestCaseProperty(long id, long caseId, String name, String value)
        {
            this.id = id;
            this.caseId = caseId;
            this.name = name;
            this.value = value;
        }
    }
}
