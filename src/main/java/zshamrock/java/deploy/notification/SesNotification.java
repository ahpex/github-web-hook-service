package zshamrock.java.deploy.notification;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClient;
import com.amazonaws.services.simpleemail.model.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SesNotification {
    private static final Logger logger = LoggerFactory.getLogger(SesNotification.class);

    private static String ACCESS_KEY;
    private static String ACCESS_SECRET;
    private static String SENDER;
    private static List<String> RECIPIENTS;

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private AmazonSimpleEmailServiceAsyncClient sesAsyncClient;

    public SesNotification() {
        loadProperties();

        initializeSesAsyncClient();

        registerShutdownHook();
    }

    private void loadProperties() {
        Properties properties = new Properties();
        InputStream input = this.getClass().getClassLoader().getResourceAsStream("amazon.properties");
        try {
            properties.load(input);
            ACCESS_KEY = properties.getProperty("amazon.access.key");
            ACCESS_SECRET = properties.getProperty("amazon.access.secret");
            SENDER = properties.getProperty("amazon.ses.sender");

            RECIPIENTS = Arrays.asList(properties.getProperty("amazon.ses.recipients").split(","));
        } catch (IOException e) {
            throw new UnableToReadAmazonPropertiesException(e);
        }
        IOUtils.closeQuietly(input);
    }

    private void initializeSesAsyncClient() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, ACCESS_SECRET);

        ClientConfiguration clientConfiguration = new ClientConfiguration();

        sesAsyncClient = new AmazonSimpleEmailServiceAsyncClient(credentials, clientConfiguration, EXECUTOR);
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new SesShutdown());
    }

    public void sendNotification(String data) {
        Content subject = new Content("Deploy is triggered!");
        Body body = new Body(new Content(data));
        Message message = new Message(subject, body);

        SendEmailRequest emailRequest =
                new SendEmailRequest(SENDER, new Destination(RECIPIENTS), message);
        sesAsyncClient.sendEmailAsync(emailRequest);
    }

    private static final class UnableToReadAmazonPropertiesException extends RuntimeException {
        private UnableToReadAmazonPropertiesException(Throwable cause) {
            super(cause);
        }
    }

    private class SesShutdown extends Thread {
        @Override
        public void run() {
            logger.info("Doing SES Shutdown...");

            EXECUTOR.shutdown();
            try {
                EXECUTOR.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("awaitTermination was interrupted!", e);
            }
            sesAsyncClient.shutdown();

            logger.info("Shutdown done!");
        }
    }
}
