package platform.connection;

import common.Messages;
import common.connection.CommandDecoder;
import common.connection.CommandEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

@Service
public class Connection {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    protected NioEventLoopGroup bossGroup;
    protected NioEventLoopGroup workerGroup;

    @Value("${Connection.acceptorsCount:4}")
    private int acceptorsCount;

    @Value("${Connection.port:5000}")
    private int port;

    private final CommandEncoder commandEncoder;

    private MessageHandler messageHandler;

    public Connection(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
        commandEncoder = new CommandEncoder(Messages.registeredMessages);
    }

    @PostConstruct
    public void init() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(acceptorsCount);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class);
        bootstrap.handler(new LoggingHandler(LogLevel.DEBUG));

        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();

                pipeline.addLast("decoder", new CommandDecoder(Messages.registeredMessages));
                pipeline.addLast("encoder", commandEncoder);
                pipeline.addLast("messageHandler", messageHandler);
            }
        });

        // Bind and start to accept incoming connections.
        ChannelFuture future = bootstrap.bind(new InetSocketAddress(port));
        future.addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                log.info("SocketConnection bound to {}", port);
            } else {
                log.error("SocketConnection bound attempt failed to {}! {}", port, channelFuture.cause().toString());
                System.exit(-1);
            }
        });
    }

    @PreDestroy
    public void stop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().syncUninterruptibly();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully().syncUninterruptibly();
        }
    }

}
