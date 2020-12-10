package server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import server.domain.UserProfile;

@Service
public class StartGameService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("#{costOfGame}")
    Integer costOfGame;

    UserProfileDao userProfileDao;

    @Autowired
    public StartGameService(UserProfileDao userProfileDao) {
        this.userProfileDao = userProfileDao;
    }

    public void purchaseGame(String uid) {
        UserProfile user = (UserProfile) userProfileDao.findUserProfileByUid(uid);
        user.setEnergy(user.getEnergy() - costOfGame);
        userProfileDao.updateUserProfile(user);
    }
}
