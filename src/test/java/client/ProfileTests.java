package client;

import common.messages.FinishGameRequest;
import common.messages.FinishGameResponse;
import common.messages.StartGameRequest;
import common.messages.StartGameResponse;
import common.util.MessageUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import platform.service.UserProfileRegistry;
import server.ServerApplication;
import server.domain.UserProfile;
import server.service.ProfileService;

import javax.annotation.Resource;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ServerApplication.class)
@TestPropertySource("/application-test.properties")
public class ProfileTests extends ConnectAndLoginTests {

    @Resource
    ProfileService profileService;

    @MockBean
    MessageUtil messageUtil;

    private UserProfile profile;

    @Value("${errorMessage}")
    String errorMessage;

    @Test
    public void withdrawEnergyByStartGameTest() {
        successLoginTest();
        profile = profileService.selectUserProfile(enterAccount.userProfile.id);

        assertSame(25, profile.getEnergy());
        assertSame(100, profile.getMoney());

        IntStream.rangeClosed(1, 5).forEach(i -> {
            var startGameResponse = request(new StartGameRequest(), StartGameResponse.class);
            assertSame(0, startGameResponse.errorCode);
        });
        var startGameResponse = request(new StartGameRequest(), StartGameResponse.class);
        assertSame(1, startGameResponse.errorCode);
    }

    // mock method
    public StartGameResponse request(StartGameRequest message, Class<StartGameResponse> responseClass) {
        if (profile.getEnergy() >= 5) {
            profile.setEnergy(profile.getEnergy() - 5);

            return new StartGameResponse();
        } else {
            var startGameResponse = new StartGameResponse();
            startGameResponse.errorCode = 1;
            startGameResponse.errorMessage = errorMessage;

            return startGameResponse;
        }
    }

    @Test
    public void withdrawEnergyByStartGameTestRequestShouldNotPassedAndReturnErrorMessageAndEnergyEqualZeroAndCodeError() {
//        successLoginTest();
        StartGameRequest startGameRequest = new StartGameRequest();

        //WHEN
        when((messageUtil).isRequestDuplicate(startGameRequest)).thenReturn(false);
        IntStream.rangeClosed(1, 5).forEach(i -> {
            var startGameResponse = clientConnection.request(new StartGameRequest(), StartGameResponse.class);
        });

        StartGameResponse response = clientConnection.request(startGameRequest, StartGameResponse.class);

        //THEN
        profile = profileService.selectUserProfile(1);
        assertSame(0, profile.getEnergy());
        assertSame(1, response.errorCode);
        assertEquals(errorMessage, response.errorMessage);


        clientConnection.request(new FinishGameRequest(), FinishGameResponse.class);
    }

    @Test
    public void withdrawEnergyByStartGameTestWithRealRequestShouldPassedAndEnergyBeChangedInUserProfileDataBase() {
//        successLoginTest();

        //WHEN
        StartGameResponse response = clientConnection.request(new StartGameRequest(), StartGameResponse.class);

        //THEN
        profile = profileService.selectUserProfile(1);
        assertSame(20, profile.getEnergy());
        assertSame(0, response.errorCode);


        clientConnection.request(new FinishGameRequest(), FinishGameResponse.class);
    }

}
