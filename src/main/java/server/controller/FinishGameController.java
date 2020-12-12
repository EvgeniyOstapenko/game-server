package server.controller;

import common.messages.FinishGameRequest;
import common.messages.FinishGameResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import platform.service.MessageController;
import server.domain.UserProfile;

@Service
public class FinishGameController implements MessageController<FinishGameRequest, UserProfile> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Object onMessage(FinishGameRequest startGameRequest, UserProfile userProfile) {
        return new FinishGameResponse();
    }

    @Override
    public Class<FinishGameRequest> messageClass() {
        return FinishGameRequest.class;
    }
}
