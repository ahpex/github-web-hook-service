package zshamrock.java.deploy.notification;

import org.junit.Test;

public class SesNotificationTest {
    @Test
    public void sendNotification() {
        new SesNotification().sendNotification("Test notification");
    }
}
