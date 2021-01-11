package server.controller;

import common.messages.FinishGameRequest;
import common.messages.FinishGameResponse;
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
public class FinishGameController implements MessageController<FinishGameRequest, UserProfile> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    ProfileService profileService;

    @Value("#{duplicateMessageStateExceptionMessage}")
    private String errorMessage;

    @Value("${statusError}")
    Integer STATUS_ERROR;

    @Autowired
    public FinishGameController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Override
    public Object onMessage(FinishGameRequest finishGameRequest, UserProfile userProfile) {

        userProfile.setState(ProfileState.MAIN_MENU);
        return profileService.takeActionsOnFinishGame(finishGameRequest, userProfile);
    }

    @Override
    public Class<FinishGameRequest> messageClass() {
        return FinishGameRequest.class;
    }
}
