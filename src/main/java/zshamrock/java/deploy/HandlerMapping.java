package zshamrock.java.deploy;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class HandlerMapping {
    private static final DeployHandler DEPLOY_HANDLER = new DeployHandler();

    public RequestHandler getHandler(HttpRequest request) {
        if (HttpMethod.POST.equals(request.getMethod()) && request.getUri().equals("/deploy")) {
            return DEPLOY_HANDLER;
        }
        return null;
    }
}
