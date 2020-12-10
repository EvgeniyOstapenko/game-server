package common.util;

import common.exception.DuplicateMessageStateException;
import common.messages.FinishGameResponse;
import common.messages.StartGameResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;

@Component
public class MessageUtil {

    private static final ArrayDeque<Object> requests = new ArrayDeque<>(1);

    @Value("#{duplicateMessageStateExceptionMessage}")
    private String errorMessage;

    public void checkStartOrFinishDuplicateState(Object request) throws DuplicateMessageStateException {
        if (!isRequestDuplicate(request)) throw new DuplicateMessageStateException(errorMessage, request);
    }

    private static boolean isRequestDuplicate(Object message) {
        if (message instanceof StartGameResponse || message instanceof FinishGameResponse) {
            if (requests.isEmpty()) {
                requests.push(message);
                return false;
            }

            if (!requests.peek().getClass().equals(message.getClass())) {
                requests.remove();
                requests.push(message);
                return false;
            }
        }
        return true;
    }
}
