package server.service;

import common.dto.AwardStructure;
import common.messages.FinishGameRequest;
import common.messages.FinishGameResponse;
import common.messages.StartGameResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import platform.service.UserProfileRegistry;
import server.common.GameResult;
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

    public StartGameResponse withdrawEnergyByStartGame(Integer userId) {
        StartGameResponse response = getResponse(userId);
        return response;
    }

    public FinishGameResponse finishGame(FinishGameRequest request, Integer userId) {
        if (request.getResult().equals(GameResult.WIN)) {
            return takeWinningActions(userId);
        }
        return takeLosingActions(userId);
    }

    private FinishGameResponse takeWinningActions(Integer userId) {
        UserProfile user = (UserProfile) userProfileRegistry.selectUserProfile(userId);
        user.setExperience(user.getExperience() + 10);
        user.setMoney(user.getMoney() + 10);
        user.setRating(user.getRating() + 3);

        return new FinishGameResponse();
    }

    private FinishGameResponse takeLosingActions(Integer userId) {
        UserProfile user = (UserProfile) userProfileRegistry.selectUserProfile(userId);
        user.setExperience(user.getExperience() + 3);

        if (user.getRating() >= 0) {
            user.setRating(user.getRating() - 1);
            userProfileRegistry.updateUserProfile(user);

            return new FinishGameResponse();
        }

        var finishGameResponse = new FinishGameResponse();
        finishGameResponse.errorCode = 1;
        finishGameResponse.errorMessage = "Not enough rating!";

        return finishGameResponse;
    }

    private StartGameResponse getResponse(Integer userId) {
        UserProfile user = (UserProfile) userProfileRegistry.selectUserProfile(userId);
        if (user.getEnergy() >= costOfGame) {
            user.setEnergy(user.getEnergy() - costOfGame);
            userProfileRegistry.updateUserProfile(user);

            return new StartGameResponse();
        }

        var startGameResponse = new StartGameResponse();
        startGameResponse.errorCode = 1;
        startGameResponse.errorMessage = "Not enough energy!";

        return startGameResponse;
    }


}
