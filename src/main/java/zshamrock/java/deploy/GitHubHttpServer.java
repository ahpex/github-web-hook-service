package zshamrock.java.deploy;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpServerCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class GitHubHttpServer {
    private static final Logger logger = LoggerFactory.getLogger(GitHubHttpServer.class);

    public GitHubHttpServer(final HandlerMapping handlerMapping) {

        NioServerSocketChannelFactory channelFactory = new NioServerSocketChannelFactory();
        ServerBootstrap server = new ServerBootstrap(channelFactory);
        server.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("http server codec", new HttpServerCodec());
                pipeline.addLast("http client codec", new HttpClientCodec());
                pipeline.addLast("http aggregator", new HttpChunkAggregator(512 * 1024)); // 512kb
                pipeline.addLast("github service hook handler", new GitHubPushServiceHookHandler(handlerMapping));

                return pipeline;
            }
        });

        server.bind(new InetSocketAddress("localhost", 9091));
    }

    private static class GitHubPushServiceHookHandler extends SimpleChannelUpstreamHandler {
        private HandlerMapping handlerMapping;

        private GitHubPushServiceHookHandler(HandlerMapping handlerMapping) {
            this.handlerMapping = handlerMapping;
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            HttpRequest request = (HttpRequest) e.getMessage();

            logger.info("Processing request: URI->{}, method->{}", request.getUri(), request.getMethod().getName());
            RequestHandler handler = handlerMapping.getHandler(request);
            if (handler != null) {
                handler.handle(request);
            } else {
                logger.warn("No request handler is found!");
            }

            Channel channel = e.getChannel();
            if (channel.isWritable()) {
                channel.write(ChannelBuffers.wrappedBuffer("OK".getBytes()));
                channel.close();
            }
        }
    }


}
