package org.opendcs.kiwi;

import java.io.File;
import java.time.Duration;

import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * Helper to let downstream tests get a KiwiTcms instance going.
 *
 * Manual check:
 * <pre>
 * docker compose -f lib/src/test/resources/kiwi/docker-compose.yml up -d
 * docker compose -f lib/src/test/resources/kiwi/docker-compose.yml exec web /Kiwi/manage.py migrate --noinput
 * docker compose -f lib/src/test/resources/kiwi/docker-compose.yml exec web /Kiwi/manage.py createsuperuser
 * docker compose -f lib/src/test/resources/kiwi/docker-compose.yml port web 8443
 * </pre>
 * Open https://localhost:&lt;port&gt; and log in with the user created above.
 */
public class KiwiTestContainer extends ComposeContainer
{
    public static final String DEFAULT_USERNAME = "admin";
    public static final String DEFAULT_PASSWORD = "admin";

    private static final String WEB_SERVICE = "web-1";
    private static final int HTTPS_PORT = 8443;

    public KiwiTestContainer(File composeFile)
    {
        super(composeFile);
        withExposedService(WEB_SERVICE, HTTPS_PORT,
                Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)));
    }

    public String getKiwiUrl()
    {
        return "https://" + super.getServiceHost(WEB_SERVICE, HTTPS_PORT)
                + ":"
                + super.getServicePort(WEB_SERVICE, HTTPS_PORT);
    }

    public void createDefaultUser() throws Exception
    {
        createUser(DEFAULT_USERNAME, DEFAULT_PASSWORD, "admin@example.com");
    }

    public void createUser(String username, String password, String email) throws Exception
    {
        ContainerState web = getContainerByServiceName(WEB_SERVICE)
                .orElseThrow(() -> new IllegalStateException("Kiwi web container was not started."));
        Container.ExecResult migration = web.execInContainer("/Kiwi/manage.py", "migrate", "--noinput");
        assertSuccessful(migration, "migrate");

        // Use Django's user model so password hashing and required fields stay Kiwi-compatible.
        String script = "from django.contrib.auth import get_user_model; "
                + "User = get_user_model(); "
                + "User.objects.filter(username='" + pythonString(username) + "').delete(); "
                + "User.objects.create_superuser('" + pythonString(username) + "', '"
                + pythonString(email) + "', '" + pythonString(password) + "')";
        Container.ExecResult user = web.execInContainer("/Kiwi/manage.py", "shell", "-c", script);
        assertSuccessful(user, "create superuser");
    }

    private static String pythonString(String value)
    {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    private static void assertSuccessful(Container.ExecResult result, String command)
    {
        if (result.getExitCode() != 0)
        {
            throw new IllegalStateException("Kiwi " + command + " failed: " + result.getStderr());
        }
    }
}
