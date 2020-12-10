package common.util;

import common.messages.FinishGameResponse;
import common.messages.StartGameResponse;

import java.util.ArrayDeque;

public class MessageUtil {

    private static ArrayDeque<Object> requests = new ArrayDeque<>(1);

    public static boolean isRequestDuplicate(Object message){
        if(message instanceof StartGameResponse || message instanceof FinishGameResponse){
            if(requests.isEmpty()) {
                requests.push(message);
                return false;
            }

            if(!requests.peek().getClass().equals(message.getClass())){
                requests.remove();
                requests.push(message);
                return false;
            }

        }
        return true;
    }
}
