package org.opendcs.testing.kiwi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestCase
{
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


    private TestCase(String summary, String steps, Priority priority,
                     Product product, Category category, String requirements,
                     String notes, String arguments, String referenceLink,
                     String status, List<Attachment> attachments, List<String> tags,
                     List<Component> components)
    {
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
        
    }

    public static class Builder
    {
        private List<Attachment> attachments = new ArrayList<>();
        private List<String> tags = new ArrayList<>();
        private List<Component> components = new ArrayList<>();

        public TestCase build()
        {
            return new TestCase(null, null, null, null,
                                null,null,null,null,null,null,
                                attachments, tags, components);
        }
    }

    public String getSummary()
    {
        return summary;
    }

    public String getSteps()
    {
        return steps;
    }

    public Priority getPriority()
    {
        return priority;
    }

    public Product getProduct()
    {
        return product;
    }

    public Category getCategory()
    {
        return category;
    }

    public String getRequirements()
    {
        return requirements;
    }

    public String getNotes()
    {
        return notes;
    }
    
    public String getArguments()
    {
        return arguments;
    }

    public String getReferenceLink()
    {
        return referenceLink;
    }

    public String getStatus()
    {
        return status;
    }

    public List<Attachment> getAttachments()
    {
        return Collections.unmodifiableList(attachments);
    }

    public List<String> getTags()
    {
        return Collections.unmodifiableList(tags);
    }

    public List<Component> getComponents()
    {
        return Collections.unmodifiableList(components);
    }
}
