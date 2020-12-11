package common;

import common.messages.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface Messages {

    Map<Short, Class<?>> registeredMessages = Collections.unmodifiableMap(new HashMap<>(){{
        int i = 1;
        put((short) i++,  Ping.class);
        put((short) i++,  Pong.class);
        put((short) i++,  Login.class);
        put((short) i++,  LoginError.class);
        put((short) i++,  EnterAccount.class);
        put((short) i++,  StartGameRequest.class);
        put((short) i++,  StartGameResponse.class);
        put((short) i++,  FinishGameRequest.class);
        put((short) i++,  FinishGameResponse.class);
        put((short) i++,  TopRequest.class);
        put((short) i++,  TopResponse.class);
        put((short) i++,  ErrorResponse.class);
    }});

}
