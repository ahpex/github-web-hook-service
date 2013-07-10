package zshamrock.java.deploy;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DeployHandler implements RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(DeployHandler.class);

    private static final Executor EXECUTOR =  Executors.newFixedThreadPool(5);

    @Override
    public void handle(HttpRequest request) {
        String message = getMessage(request);

        if (isDeployBranch(message)) {
            EXECUTOR.execute(new DeployScript());
        } else {
            logger.info("Got push service hook for non deploy branch.");
        }
    }

    private String getMessage(HttpRequest request) {
        ChannelBuffer content = request.getContent();
        int length = content.readableBytes();
        byte[] data = new byte[length];
        content.getBytes(0, data, 0, length);

        String message = new String(data, 0, length);
        logger.info("Got message: {}", message);

        return message;
    }

    protected boolean isDeployBranch(String payload) {
        return payload.contains("\"ref\": \"refs/heads/deploy\"");
    }

    private static class DeployScript implements Runnable {

        @Override
        public void run() {
            try {
                logger.info("Executing deploy script...");
                Process process = Runtime.getRuntime().exec("/var/log/github/deploy/deploy.sh");
                int exitStatus;
                try {
                    exitStatus = process.waitFor();
                } catch (InterruptedException e) {
                    logger.error("Script was interrupted", e);
                    return;
                }
                if (exitStatus == 0) {
                    logger.info("\texecuted successfully");
                } else {
                    logger.error("\texecution failed: {}", process.exitValue());
                }
            } catch (IOException ex) {
                logger.error("Exception during deploy script execution", ex);
            }
        }
    }
}
