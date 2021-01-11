package server.controller;

import common.messages.StartGameRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import platform.service.MessageController;
import server.common.ProfileState;
import server.domain.UserProfile;
import server.service.ProfileService;

@Service
public class StartGameController implements MessageController<StartGameRequest, UserProfile> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());


    ProfileService profileService;

    @Value("#{duplicateMessageStateExceptionMessage}")
    private String errorMessage;

    @Autowired
    public StartGameController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Override
    public Object onMessage(StartGameRequest startGameRequest, UserProfile userProfile) {
        userProfile.setState(ProfileState.IN_GAME);
        return profileService.takeActionsOnStartGame(userProfile);
    }

    @Override
    public Class<StartGameRequest> messageClass() {
        return StartGameRequest.class;
    }

}
