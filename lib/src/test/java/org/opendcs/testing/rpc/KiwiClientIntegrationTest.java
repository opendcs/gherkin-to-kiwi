package org.opendcs.testing.rpc;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.opendcs.kiwi.KiwiTestContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@EnabledIfSystemProperty(named = "opendcs.test.kiwi_integration", matches = "true")
public class KiwiClientIntegrationTest
{
    @Container
    private static final KiwiTestContainer KIWI = new KiwiTestContainer(
            new File("src/test/resources/kiwi/docker-compose.yml"));

    @Test
    public void can_login_to_kiwi() throws Exception
    {
        KIWI.createDefaultUser();

        assertDoesNotThrow(() -> new KiwiClient(KIWI.getKiwiUrl(),
                KiwiTestContainer.DEFAULT_USERNAME,
                KiwiTestContainer.DEFAULT_PASSWORD));
    }
}
