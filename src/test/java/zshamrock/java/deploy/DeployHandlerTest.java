package zshamrock.java.deploy;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DeployHandlerTest {
    @Test
    public void isDeployBranch() {
        boolean deployBranch =
                new DeployHandler().isDeployBranch(" \"pusher\": { \"name\": \"none\" }, \"ref\": \"refs/heads/deploy\", \"repository\": { \"created_at\": 1351526997}");

        assertThat(deployBranch, is(true));
    }

    @Test
    public void isNotDeployBranch() {
        boolean deployBranch =
                new DeployHandler().isDeployBranch(" \"pusher\": { \"name\": \"none\" }, \"ref\": \"refs/heads/master\", \"repository\": { \"created_at\": 1351526997}");

        assertThat(deployBranch, is(false));
    }
}
