package platform.connection;

import common.util.KeyValue;
import common.util.MessageUtil;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import platform.messages.ILogin;
import platform.service.AuthService;
import platform.service.LoginController;
import platform.service.MessageController;
import platform.service.UserProfileRegistry;
import platform.session.Session;
import platform.session.SessionMap;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service
@ChannelHandler.Sharable
public class MessageHandler extends ChannelInboundHandlerAdapter {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MessageUtil messageUtil;

    @Autowired
    UserProfileRegistry userProfileRegistry;

    @Resource
    private AuthService authService;

    @Resource
    private LoginController loginController;

    @Resource
    private SessionMap sessionMap;

    @Value("${statusError}")
    Integer STATUS_ERROR;

    @Value("${freeQueueSlots}")
    private int FREE_QUEUE_SLOTS = 100;

    private Map<Channel, Session> openConnections = new ConcurrentHashMap<>();

    private Map<Class, MessageController> controllers = Collections.emptyMap();

    private PendingWriteQueue pendingQueue;


    @Autowired(required = false)
    private void setControllers(List<MessageController> controllers) {
        this.controllers = controllers.stream().collect(Collectors.toMap(MessageController::messageClass, c -> c));
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.pendingQueue = new PendingWriteQueue(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Channel CONNECTED: {}", ctx.channel());
        }
        openConnections.put(ctx.channel(), Session.EMPTY_SESSION);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, final Object message) throws Exception {

        var channel = ctx.channel();
        if (message instanceof ILogin) {
            var error = new KeyValue<Integer, String>();
            var session = authService.authorize((ILogin) message, channel, error);
            if (session != null) {
                openConnections.put(channel, session);
                var outMessages = loginController.onSuccessLogin(session.profile);
                for (Object m : outMessages) {
                    this.pendingQueue.add(m, new DefaultChannelPromise(ctx.channel()));
                }

            } else {
                this.pendingQueue.add(loginController.onLoginError((ILogin) message, error), new DefaultChannelPromise(ctx.channel()));
            }
        } else {
            var messageController = controllers.get(message.getClass());
            if (messageController != null) {
                Object outMessage = messageController.onMessage(message, openConnections.get(channel).profile);
                if (outMessage != null) {
                    this.pendingQueue.add(outMessage, new DefaultChannelPromise(ctx.channel()));
                }
            } else {
                log.error("Controller for message of class [{}] not found!", message.getClass());
            }
        }
        sendMessagesThroughQueue(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        log.error(cause.toString(), cause);
        channel.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Channel CLOSED: {}", ctx.channel());
        }
        var session = openConnections.remove(ctx.channel());
        if (session != null && session.profile != null) {
            sessionMap.closeSession(session.profile.id());
        }
    }

    private synchronized void sendMessagesThroughQueue(ChannelHandlerContext ctx) {
        if (this.FREE_QUEUE_SLOTS > 0) {
            while (this.FREE_QUEUE_SLOTS > 0) {
                if (this.pendingQueue.removeAndWrite() == null) {
                    ctx.flush();
                    return;
                }
                this.FREE_QUEUE_SLOTS--;
            }
        }
    }

}
