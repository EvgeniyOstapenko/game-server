package server.controller;

import common.messages.ChangeUserNameRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import platform.service.MessageController;
import server.domain.UserProfile;
import server.service.ProfileService;

@Service
public class ChangeUserNameController implements MessageController<ChangeUserNameRequest, UserProfile> {

    ProfileService profileService;

    @Autowired
    public ChangeUserNameController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Override
    public Object onMessage(ChangeUserNameRequest changeUserNameRequest, UserProfile userProfile) {
        return profileService.getChangeUserNameResponse(userProfile, changeUserNameRequest.getNewUserName());
    }

    @Override
    public Class<ChangeUserNameRequest> messageClass() {
        return ChangeUserNameRequest.class;
    }
}
