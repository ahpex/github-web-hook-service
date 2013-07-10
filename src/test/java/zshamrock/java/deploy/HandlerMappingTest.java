package zshamrock.java.deploy;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class HandlerMappingTest {
    @Mock
    private HttpRequest request;

    @Test
    public void isDeployHandler() {
        given(request.getMethod()).willReturn(HttpMethod.POST);
        given(request.getUri()).willReturn("/deploy");

        RequestHandler handler = new HandlerMapping().getHandler(request);

        assertThat(handler, is(instanceOf(DeployHandler.class)));
    }

    @Test
    public void isNotDeployHandlerBecauseOfGetRequest() {
        given(request.getMethod()).willReturn(HttpMethod.GET);
        given(request.getUri()).willReturn("/deploy");

        RequestHandler handler = new HandlerMapping().getHandler(request);

        assertThat(handler, is(nullValue()));
    }

    @Test
    public void isNotDeployHandlerBecauseOfDifferentURI() {
        given(request.getMethod()).willReturn(HttpMethod.POST);
        given(request.getUri()).willReturn("/");

        RequestHandler handler = new HandlerMapping().getHandler(request);

        assertThat(handler, is(nullValue()));
    }
}
