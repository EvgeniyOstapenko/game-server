package server.controller;

import common.messages.StartGameRequest;
import common.messages.StartGameResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import platform.service.MessageController;
import server.domain.UserProfile;

@Service
public class StartGameController implements MessageController<StartGameRequest, UserProfile> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Object onMessage(StartGameRequest startGameRequest, UserProfile userProfile) {
        return new StartGameResponse();
    }

    @Override
    public Class<StartGameRequest> messageClass() {
        return StartGameRequest.class;
    }
}
