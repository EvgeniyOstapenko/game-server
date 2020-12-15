package platform.service;

import common.util.KeyValue;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import platform.messages.ILogin;
import platform.session.Session;
import platform.session.SessionMap;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class AuthService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private UserProfileRegistry userProfileRegistry;

    @Resource
    private LoginValidator loginValidator;

    @Resource
    private SessionMap sessionMap;

    public Session authorize(ILogin login, Channel channel, KeyValue<Integer, String> error) {
        var validate = loginValidator.validate(login, error);
        if (!validate) {
            return null;
        }
        var profile = userProfileRegistry.findUserProfileByUid(login.uid());
        Session session;
        if (profile != null) {
            session = sessionMap.getSessionByProfileId(profile.id());
            if (session != null && !Objects.equals(session.channel, channel)) {
                error.key = 2;
                error.value = "OtherSessionAlreadyExists";
                session.channel.close();
                return null;
            }
            session = sessionMap.createNewSession(profile, channel);
        } else {
            var newUserProfile = userProfileRegistry.createNewUserProfile(login.uid());
            session = sessionMap.createNewSession(newUserProfile, channel);
        }
        return session;
    }

}
