package org.opendcs.testing.kiwi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TestPlan {
    private final String name;
    private final long id;
    private final Product product;
    private final Version version;
    private final Type type;
    private final List<TestCase> cases;
    

    private TestPlan(long id, String name, Product product, Version version, Type type, List<TestCase> cases) {
        this.id = id;
        this.name = name;
        this.product = product;
        this.version = version;
        this.type = type;
        this.cases = cases;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public Version getVersion() {
        return version;
    }

    public Type getType() {
        return type;
    }

    public List<TestCase> getCases() {
        return Collections.unmodifiableList(cases);
    }

    public TestPlan.Builder newBuilder() {
        return new Builder(this);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("testplan{");
        sb.append("Id=").append(getId()).append(",");
        sb.append("Name=").append(getName()).append(",");
        sb.append("Version=").append(getVersion()).append(",");
        sb.append("Product=").append(getProduct().name).append(",");
        sb.append("Type=").append(getType().toString()).append(",");
        sb.append("Cases=")
          .append(
            cases.stream()
                 .map(tc -> tc.getSummary())
                 .collect(Collectors.joining(","))
        );
        return sb.append("}").toString();
    }

    public static class Builder {
        private long id = -1;
        private String name = null;
        private Product product = null;
        private Type type = null;
        private List<TestCase> cases = new ArrayList<>();
        private Version version = null;

        public Builder() {

        }

        public Builder(TestPlan tp) {
            this.id = tp.id;
            this.name = tp.name;
            this.product = tp.product;
            this.version = tp.version;
            this.type = tp.type;
            this.cases.addAll(tp.getCases());
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withProduct(Product product) {
            this.product = product;
            return this;
        }

        public Builder withVersion(Version version) {
            this.version = version;
            return this;
        }

        public Builder withVersion(String name) {
            this.version = Version.of(name, this.product);
            return this;
        }

        public Builder withType(String type) {
            this.type = Type.of(type);
            return this;
        }

        public Builder withType(Type type) {
            this.type = type;
            return this;
        }

        public Builder withTest(TestCase tc) {
            cases.add(tc);
            return this;
        }

        public TestPlan build() {
            return new TestPlan(id, name, product, Version.of(version.name, product), type, cases);
        }
    }
}
