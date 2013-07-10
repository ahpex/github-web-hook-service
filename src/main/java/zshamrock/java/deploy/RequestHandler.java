package zshamrock.java.deploy;

import org.jboss.netty.handler.codec.http.HttpRequest;

public interface RequestHandler {
    public void handle(HttpRequest request);
}
