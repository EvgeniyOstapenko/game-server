package server.controller;

import common.messages.StartGameRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import platform.service.MessageController;
import server.domain.UserProfile;
import server.service.ProfileService;

@Service
public class StartGameController implements MessageController<StartGameRequest, UserProfile> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());


    ProfileService profileService;

    @Autowired
    public StartGameController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Override
    public Object onMessage(StartGameRequest startGameRequest, UserProfile userProfile) {
        return profileService.takeActionsOnStartGame(userProfile.id());
    }

    @Override
    public Class<StartGameRequest> messageClass() {
        return StartGameRequest.class;
    }

}
