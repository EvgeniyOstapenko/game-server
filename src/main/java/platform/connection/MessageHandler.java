package platform.connection;

import common.exception.DuplicateMessageStateException;
import common.messages.AbstractResponse;
import common.util.KeyValue;
import common.util.MessageUtil;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import platform.domain.IUser;
import platform.messages.ILogin;
import platform.service.AuthService;
import platform.service.LoginController;
import platform.service.MessageController;
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

    @Resource
    private AuthService authService;

    @Resource
    private LoginController loginController;

    @Resource
    private SessionMap sessionMap;

    @Value("${statusError}")
    Integer STATUS_ERROR;

    private Map<Channel, Session> openConnections = new ConcurrentHashMap<>();

    private Map<Class, MessageController> controllers = Collections.emptyMap();

    private Object message;

    @Autowired(required = false)
    private void setControllers(List<MessageController> controllers) {
        this.controllers = controllers.stream().collect(Collectors.toMap(MessageController::messageClass, c -> c));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Channel CONNECTED: {}", ctx.channel());
        }
        openConnections.put(ctx.channel(), Session.EMPTY_SESSION);
    }

    private PendingWriteQueue queue;
    private int freeSlots = 10;

    private synchronized void trySendMessages(ChannelHandlerContext ctx) {
        if(this.freeSlots > 0) {
            while(this.freeSlots > 0) {
                if(this.queue.removeAndWrite() == null) {
                    ctx.flush();
                    return;
                }
                this.freeSlots--;
            }

        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, final Object message) throws Exception {

        queue = new PendingWriteQueue(ctx);
        this.message = message;

//        this.queue.add(message, new DefaultChannelPromise(ctx.channel()));
//        trySendMessages(ctx);

        var channel = ctx.channel();
        if (message instanceof ILogin) {
            var error = new KeyValue<Integer, String>();
            var session = authService.authorize((ILogin) message, channel, error);
            if (session != null) {
                openConnections.put(channel, session);
                var outMessages = loginController.onSuccessLogin(session.profile);
                outMessages.forEach(channel::write);
                channel.flush();

//                for (Object m : outMessages) {
//                    this.queue.add(message, new DefaultChannelPromise(ctx.channel()));
//                }
//                trySendMessages(ctx);


            } else {
//                channel.writeAndFlush(loginController.onLoginError((ILogin) message, error));
            }
        } else {
            var messageController = controllers.get(message.getClass());
            if (messageController != null) {
                var outMessage = getResponseMessage(message, messageController, channel);
                if (outMessage != null) {
//                    channel.writeAndFlush(outMessage);
                }
            } else {
                log.error("Controller for message of class [{}] not found!", message.getClass());
            }
        }
    }

    public void channelRead1(ChannelHandlerContext ctx, final Object message){

        queue = new PendingWriteQueue(ctx);
        this.message = message;

        this.queue.add(message, new DefaultChannelPromise(ctx.channel()));
        trySendMessages(ctx);

        var channel = ctx.channel();
        if (message instanceof ILogin) {
            var error = new KeyValue<Integer, String>();
            var session = authService.authorize((ILogin) message, channel, error);
            if (session != null) {
                openConnections.put(channel, session);
                var outMessages = loginController.onSuccessLogin(session.profile);
                outMessages.forEach(channel::write);
                channel.flush();
            } else {
                channel.writeAndFlush(loginController.onLoginError((ILogin) message, error));
            }
        } else {
            var messageController = controllers.get(message.getClass());
            if (messageController != null) {
                var outMessage = getResponseMessage(message, messageController, channel);
                if (outMessage != null) {
                    channel.writeAndFlush(outMessage);
                }
            } else {
                log.error("Controller for message of class [{}] not found!", message.getClass());
            }
        }
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

    private void validateStartGameAndFinishGAmeRequests(Object message, ChannelHandlerContext ctx) {
        try {
            messageUtil.checkStartOrFinishDuplicateState(message);
        } catch (DuplicateMessageStateException error) {
            String errorMessage = error.getReason();
            log.error(errorMessage, message.getClass());

            Channel channel = ctx.channel();
            channel.close();
        }
    }

    private Object getResponseMessage(Object message, MessageController messageController, Channel channel) {
        IUser profile = openConnections.get(channel).profile;

        if (!messageUtil.isRequestDuplicate(message)) {
            return messageController.onMessage(message, profile);
        }

        String errorMessage = toLogException(message);

        var response = messageController.onMessage(message, profile);
        AbstractResponse errorResponse = (AbstractResponse) response;
        errorResponse.errorCode = STATUS_ERROR;
        errorResponse.errorMessage = errorMessage;

        return response;
    }

    private String toLogException(Object message) {
        messageUtil.setRequest(message);
        var errorMessage = messageUtil.getStartOrFinishDuplicateStateErrorMessage();
        log.error(errorMessage, message.getClass());

        return errorMessage;
    }
}
