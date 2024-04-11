package org.opendcs.testing.kiwi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestPlan {
    private final String name;
    private final long id;
    private final Product product;
    private final String version;
    private final String type;
    private final List<TestCase> cases;
    

    private TestPlan(long id, String name, Product product, String version, String type, List<TestCase> cases) {
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

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public List<TestCase> getCases() {
        return Collections.unmodifiableList(cases);
    }

    public static class Builder {
        private long id = -1;
        private String name = null;
        private Product product = null;
        private String version = null;
        private String type = null;
        private List<TestCase> cases = new ArrayList<>();

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

        public Builder withVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withTest(TestCase tc) {
            cases.add(tc);
            return this;
        }

        public TestPlan build() {
            return new TestPlan(id, name, product, version, type, cases);
        }
    }
}
