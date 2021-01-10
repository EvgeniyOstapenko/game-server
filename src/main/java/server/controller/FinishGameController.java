package server.controller;

import common.messages.FinishGameRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import platform.service.MessageController;
import server.domain.UserProfile;
import server.service.ProfileService;

@Service
public class FinishGameController implements MessageController<FinishGameRequest, UserProfile> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    ProfileService profileService;

    @Autowired
    public FinishGameController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Override
    public Object onMessage(FinishGameRequest finishGameRequest, UserProfile userProfile) {
        return profileService.takeActionsOnFinishGame(finishGameRequest, userProfile);
    }

    @Override
    public Class<FinishGameRequest> messageClass() {
        return FinishGameRequest.class;
    }
}
