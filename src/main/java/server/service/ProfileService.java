package server.service;

import common.dto.AwardStructure;
import common.messages.FinishGameRequest;
import common.messages.FinishGameResponse;
import common.messages.StartGameResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import platform.service.UserProfileRegistry;
import server.common.GameResult;
import server.domain.InventoryItem;
import server.domain.TopItem;
import server.domain.UserProfile;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@TestPropertySource("/application.properties")
public class ProfileService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private UserProfileRegistry userProfileRegistry;

    @Autowired
    private TopService topService;

    @Value("#{levelsConfig}")
    private Map<Integer, Integer> levelsConfig;

    @Value("#{levelUpAwardConfig}")
    private Map<Integer, AwardStructure> levelUpAwardConfig;

    @Value("#{costOfGame}")
    Integer ENERGY_GAME_PRICE;

    @Value("${ratingErrorMessage}")
    String RATING_ERROR_MESSAGE;

    @Value("${energyErrorMessage}")
    String ENERGY_ERROR_MESSAGE;

    @Value("${statusError}")
    Integer STATUS_ERROR;

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

    public StartGameResponse takeActionsOnStartGame(Integer userId) {
        return getStartGameResponse(userId);
    }

    public FinishGameResponse takeActionsOnFinishGame(FinishGameRequest request, Integer userId) {
        if (request.getResult().equals(GameResult.WIN)) {
            return takeWinningActions(userId);
        }
        return takeLosingActions(userId);
    }

    private FinishGameResponse takeWinningActions(Integer userId) {
        UserProfile user = (UserProfile) userProfileRegistry.selectUserProfile(userId);
        user.setExperience(user.getExperience() + 10);
        UserProfile currentUser = recalculateUserLevel(user);

        currentUser.setMoney(currentUser.getMoney() + 10);
        currentUser.setRating(currentUser.getRating() + 3);
        userProfileRegistry.updateUserProfile(currentUser);

        return new FinishGameResponse();
    }

    private FinishGameResponse takeLosingActions(Integer userId) {
        UserProfile user = (UserProfile) userProfileRegistry.selectUserProfile(userId);
        user.setExperience(user.getExperience() + 3);
        UserProfile currentUser = recalculateUserLevel(user);

        if (currentUser.getRating() > 0) {
            currentUser.setRating(currentUser.getRating() - 1);
            userProfileRegistry.updateUserProfile(currentUser);

            return new FinishGameResponse();
        }

        var finishGameResponse = new FinishGameResponse();
        finishGameResponse.errorCode = STATUS_ERROR;
        finishGameResponse.errorMessage = RATING_ERROR_MESSAGE;

        userProfileRegistry.updateUserProfile(user);
        return finishGameResponse;
    }

    private StartGameResponse getStartGameResponse(Integer userId) {
        UserProfile user = (UserProfile) userProfileRegistry.selectUserProfile(userId);
        if (user.getEnergy() >= ENERGY_GAME_PRICE) {
            user.setEnergy(user.getEnergy() - ENERGY_GAME_PRICE);
            userProfileRegistry.updateUserProfile(user);

            return new StartGameResponse();
        }

        var startGameResponse = new StartGameResponse();
        startGameResponse.errorCode = STATUS_ERROR;
        startGameResponse.errorMessage = ENERGY_ERROR_MESSAGE;

        return startGameResponse;
    }

    private UserProfile recalculateUserLevel(UserProfile user){
        int userLevel = user.getLevel();
        int experience = user.getExperience();
        int fullUserExperience = levelsConfig.get(userLevel) + experience;

        List<Map.Entry<Integer, Integer>> userLevels = levelsConfig.entrySet()
                .stream().filter(level -> level.getValue() <= fullUserExperience).collect(Collectors.toList());

        int achievedLevel = userLevels.get(userLevels.size() - 1).getKey();

        if(userLevel < achievedLevel){
            getAwardForEachLevelReached(user, userLevel, achievedLevel);
        }

        int updatedExperience = fullUserExperience - levelsConfig.get(achievedLevel);

        user.setLevel(achievedLevel);
        user.setExperience(updatedExperience);
        userProfileRegistry.updateUserProfile(user);
        return user;
    }

    private void getAwardForEachLevelReached(UserProfile user, int oldLevel, int achievedLevel) {
        IntStream.rangeClosed(oldLevel + 1, achievedLevel).forEach(i -> {
            var award = levelUpAwardConfig.get(i);
            user.setMoney(user.getMoney() + award.getMoney());
            user.setEnergy(user.getEnergy() + award.getEnergy());

            Optional<List<InventoryItem>> awardOptional = Optional.ofNullable(award.getInventoryItems());
            awardOptional.ifPresent(aw -> aw.forEach(item -> user.getInventory().add(item)));

        });
    }


}
