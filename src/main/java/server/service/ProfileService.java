package server.service;

import common.dto.AwardStructure;
import common.messages.ChangeUserNameResponse;
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
import platform.session.SessionMap;
import server.common.GameResult;
import server.common.ProfileState;
import server.domain.InventoryItem;
import server.domain.UserProfile;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@TestPropertySource("/application.properties")
public class ProfileService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private UserProfileRegistry userProfileRegistry;

    @Autowired
    private TopRequestService topRequestService;

    @Autowired
    private SessionMap sessionMap;

    @Value("#{levelsConfig}")
    private Map<Integer, Integer> levelsConfig;

    @Value("#{levelUpAwardConfig}")
    private Map<Integer, AwardStructure> levelUpAwardConfig;

    @Value("#{costOfGame}")
    Integer ENERGY_GAME_PRICE;

    @Value("${energyErrorMessage}")
    String ENERGY_ERROR_MESSAGE;

    @Value("${changeNameErrorMassage}")
    String CHANGE_NAME_ERROR_MESSAGE;

    @Value("${statusError}")
    Integer STATUS_ERROR;

    private Map<Integer, LocalDate> nameChangeDateMap = new HashMap<>();

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

    public FinishGameResponse takeActionsOnFinishGame(FinishGameRequest request, UserProfile user) {
        if (request.getResult().equals(GameResult.WIN)) {
            return takeWinningActions(user);
        }
        return takeLosingActions(user);
    }

    public ChangeUserNameResponse getChangeUserNameResponse(UserProfile userProfile, String newUserName) {
        return changeUserName(userProfile, newUserName);
    }

    private ChangeUserNameResponse changeUserName(UserProfile userProfile, String newUserName) {
        boolean allowedNameToBeChanged = isAllowedNameToBeChanged(userProfile);

        ChangeUserNameResponse changeUserNameResponse = new ChangeUserNameResponse();

        if (!allowedNameToBeChanged) {
            changeUserNameResponse.errorCode = STATUS_ERROR;
            changeUserNameResponse.errorMessage = CHANGE_NAME_ERROR_MESSAGE;
        }

        userProfile.setName(newUserName);
        return changeUserNameResponse;
    }

    private boolean isAllowedNameToBeChanged(UserProfile user) {
        int userId = user.id();
        LocalDate currentTime = LocalDate.now();

        if (nameChangeDateMap.isEmpty()) {
            nameChangeDateMap.put(userId, currentTime);
            return true;
        }

        LocalDate previousNameChangeTime = nameChangeDateMap.get(userId);
        if (currentTime.equals(previousNameChangeTime)) {
            return false;
        }

        nameChangeDateMap.put(userId, currentTime);
        return true;
    }


    private FinishGameResponse takeWinningActions(UserProfile user) {
        user.setExperience(user.getExperience() + 10);

        UserProfile currentUser = recalculateUserLevel(user);

        currentUser.setMoney(currentUser.getMoney() + 10);
        currentUser.setRating(currentUser.getRating() + 3);

        topRequestService.onRatingChange(currentUser);
        userProfileRegistry.updateUserProfile(currentUser);

        currentUser.setState(ProfileState.MAIN_MENU);
        AwardStructure userAward = getUserAward(user);
        return new FinishGameResponse(userAward);
    }

    private FinishGameResponse takeLosingActions(UserProfile user) {
        user.setExperience(user.getExperience() + 3);
        UserProfile currentUser = recalculateUserLevel(user);

        currentUser.setState(ProfileState.MAIN_MENU);
        AwardStructure userAward = getUserAward(user);

        if (currentUser.getRating() > 0) {
            currentUser.setRating(currentUser.getRating() - 1);
            topRequestService.onRatingChange(currentUser);
        }

        userProfileRegistry.updateUserProfile(currentUser);
        return new FinishGameResponse(userAward);
    }

    private StartGameResponse getStartGameResponse(Integer userId) {
        var startGameResponse = new StartGameResponse();
        UserProfile user = (UserProfile) userProfileRegistry.selectUserProfile(userId);

        if (user.getEnergy() >= ENERGY_GAME_PRICE) {
            user.setEnergy(user.getEnergy() - ENERGY_GAME_PRICE);
            userProfileRegistry.updateUserProfile(user);

            user.setState(ProfileState.IN_GAME);
            return startGameResponse;
        }

        startGameResponse.errorCode = STATUS_ERROR;
        startGameResponse.errorMessage = ENERGY_ERROR_MESSAGE;

        return startGameResponse;
    }

    private UserProfile recalculateUserLevel(UserProfile user) {
        int userLevel = user.getLevel();
        int experience = user.getExperience();
        int fullUserExperience = levelsConfig.get(userLevel) + experience;

        List<Map.Entry<Integer, Integer>> userLevels = levelsConfig.entrySet()
                .stream().filter(level -> level.getValue() <= fullUserExperience).collect(Collectors.toList());

        int achievedLevel = userLevels.get(userLevels.size() - 1).getKey();

        if (userLevel < achievedLevel) {
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

    private AwardStructure getUserAward(UserProfile user) {
        AwardStructure userAward = new AwardStructure();
        userAward.setEnergy(user.getEnergy());
        userAward.setEnergy(user.getEnergy());
        userAward.setInventoryItems(user.getInventory());

        return userAward;
    }

}
