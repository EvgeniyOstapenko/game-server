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

    @Value("${experienceReward}")
    Integer EXPERIENCE_REWARD;

    @Value("${moneyReward}")
    Integer MONEY_REWARD;

    @Value("${ratingReward}")
    Integer RATING_REWARD;

    @Value("${lossInExperience}")
    Integer LOSS_IN_EXPERIENCE;

    @Value("${lossInRating}")
    Integer LOSS_IN_RATING;

    @Value("${ratingThreshold}")
    Integer RATING_THRESHOLD;

    @Value("${one}")
    Integer ONE;

    @Value("${finalUserLevel}")
    Integer FINAL_USER_LEVEL;

    @Value("#{duplicateMessageStateExceptionMessage}")
    private String DUPLICATE_REQUEST_ERROR_MESSAGE;

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

    public StartGameResponse takeActionsOnStartGame(UserProfile user) {
        return getStartGameResponse(user);
    }

    public FinishGameResponse takeActionsOnFinishGame(FinishGameRequest request, UserProfile user) {
        if(user.getState() == ProfileState.MAIN_MENU){
            return getDuplicateRequestErrorResponse();
        }

        user.setState(ProfileState.MAIN_MENU);

        if (request.getResult().equals(GameResult.WIN)) {
            return takeWinningActions(user);
        }
        return takeLosingActions(user);
    }

    public ChangeUserNameResponse changeUserName(UserProfile user, String newUserName) {
        boolean allowedNameToBeChanged = isAllowedNameToBeChanged(user);

        ChangeUserNameResponse changeUserNameResponse = new ChangeUserNameResponse();

        if (!allowedNameToBeChanged) {
            changeUserNameResponse.errorCode = STATUS_ERROR;
            changeUserNameResponse.errorMessage = CHANGE_NAME_ERROR_MESSAGE;
        }

        user.setName(newUserName);
        return changeUserNameResponse;
    }

    private FinishGameResponse getDuplicateRequestErrorResponse() {
        FinishGameResponse response = new FinishGameResponse();
        response.errorCode = STATUS_ERROR;
        response.errorMessage = String.format(DUPLICATE_REQUEST_ERROR_MESSAGE, new FinishGameRequest());

        return response;
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
        user.setExperience(user.getExperience() + EXPERIENCE_REWARD);

        UserProfile currentUser = recalculateUserLevel(user);

        currentUser.setMoney(currentUser.getMoney() + MONEY_REWARD);
        currentUser.setRating(currentUser.getRating() + RATING_REWARD);

        topRequestService.onRatingChange(currentUser);
        userProfileRegistry.updateUserProfile(currentUser);

        currentUser.setState(ProfileState.MAIN_MENU);
        AwardStructure userAward = getUserAward(user);
        return new FinishGameResponse(userAward);
    }

    private FinishGameResponse takeLosingActions(UserProfile user) {
        user.setExperience(user.getExperience() + LOSS_IN_EXPERIENCE);
        UserProfile currentUser = recalculateUserLevel(user);

        currentUser.setState(ProfileState.MAIN_MENU);
        AwardStructure userAward = getUserAward(user);

        if (currentUser.getRating() > RATING_THRESHOLD) {
            currentUser.setRating(currentUser.getRating() - LOSS_IN_RATING);
            topRequestService.onRatingChange(currentUser);
        }

        userProfileRegistry.updateUserProfile(currentUser);
        return new FinishGameResponse(userAward);
    }

    private StartGameResponse getStartGameResponse(UserProfile user) {
        var startGameResponse = new StartGameResponse();

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

        if (userLevel == FINAL_USER_LEVEL) {
            return user;
        }

        int fullUserExperience = levelsConfig.get(userLevel) + experience;

        List<Map.Entry<Integer, Integer>> userLevels = levelsConfig.entrySet()
                .stream().filter(level -> level.getValue() <= fullUserExperience).collect(Collectors.toList());

        int achievedLevel = userLevels.get(userLevels.size() - ONE).getKey();

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
        IntStream.rangeClosed(oldLevel + ONE, achievedLevel).forEach(i -> {
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
