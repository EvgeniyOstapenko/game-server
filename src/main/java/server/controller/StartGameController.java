package server.controller;

import common.messages.FinishGameResponse;
import common.messages.StartGameRequest;
import platform.service.MessageController;
import server.domain.UserProfile;

public class StartGameController implements MessageController<StartGameRequest, UserProfile> {
    @Override
    public Object onMessage(StartGameRequest startGameRequest, UserProfile userProfile) {
        return new FinishGameResponse();
    }

    @Override
    public Class<StartGameRequest> messageClass() {
        return StartGameRequest.class;
    }
}
