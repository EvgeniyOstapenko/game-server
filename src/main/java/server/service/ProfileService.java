package server.service;

import common.dto.AwardStructure;
import common.messages.StartGameRequest;
import common.messages.StartGameResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import platform.service.UserProfileRegistry;
import server.domain.UserProfile;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class ProfileService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private UserProfileRegistry userProfileRegistry;

    @Value("#{levelsConfig}")
    private Map<Integer, Integer> levelsConfig;

    @Value("#{levelUpAwardConfig}")
    private Map<Integer, AwardStructure> levelUpAwardConfig;

    @Value("#{costOfGame}")
    Integer costOfGame;

    public UserProfile findUserProfileOrCreateNew(String uid) {
        var profile = userProfileRegistry.findUserProfileByUid(uid);
        if (profile == null) {
            profile = userProfileRegistry.createNewUserProfile(uid);
        }
        return (UserProfile) profile;
    }

    public UserProfile selectUserProfile(int profileId) {
        return (UserProfile) userProfileRegistry.selectUserProfile(profileId);
    }

    public StartGameResponse withdrawEnergyByStartGame(Integer id) {
        UserProfile user = (UserProfile) userProfileRegistry.selectUserProfile(id);
        StartGameResponse response = request(user);
        userProfileRegistry.updateUserProfile(user);
        return response;
    }

    private StartGameResponse request(UserProfile user) {
        if (user.getEnergy() >= costOfGame) {
            user.setEnergy(user.getEnergy() - costOfGame);

            return new StartGameResponse();
        } else {
            var startGameResponse = new StartGameResponse();
            startGameResponse.errorCode = 1;
            startGameResponse.errorMessage = "Not enough energy!";

            return startGameResponse;
        }
    }



}
