package org.opendcs.testing.kiwi;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendcs.testing.rpc.KiwiClient;
import org.opendcs.testing.rpc.TestCaseRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestUtils {
    private final static Logger log = LoggerFactory.getLogger(TestUtils.class);
    private TestUtils() {}


    public static void saveTestCases(List<TestCase> cases, KiwiClient client) throws IOException {
        for (TestCase tc: cases) {
            String marker = tc.getProperty("marker");

            TestCaseRpc rpc = client.testcase();

            Map<String,String> query = new HashMap<>();
            query.put("name", "marker");
            query.put("value", marker);
            long id = rpc.properties(query)
                            .stream()
                            .map(e -> e.caseId)
                            .findFirst()
                            .orElse(-1L);
            if (id == -1)
            {
                TestCase tcOut = client.testcase().create(tc);
                long idOut = tcOut.getId();
                client.testcase().add_property(idOut, "marker", marker);
                for (Component c: tc.getComponents()) {
                    client.testcase().add_component(idOut, c, true); 
                }
                log.info("Created test, new id =" + idOut);
            }
            else
            {
                log.info("Updating test case. id = " + id);
                client.testcase().update(id, tc);
            }
        }
    }
    
}
