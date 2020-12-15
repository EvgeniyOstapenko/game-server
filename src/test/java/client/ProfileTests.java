package client;

import common.messages.FinishGameRequest;
import common.messages.FinishGameResponse;
import common.messages.StartGameRequest;
import common.messages.StartGameResponse;
import common.util.MessageUtil;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import server.ServerApplication;
import server.common.GameResult;
import server.domain.UserProfile;
import server.service.ProfileService;

import javax.annotation.Resource;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

 @SpringBootTest(classes = ServerApplication.class)
@TestPropertySource("/application-test.properties")
@Sql(value = {"/prepare-user_profile.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
 @Sql(value = {"/after-user_profile.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ProfileTests extends ConnectAndLoginTests {

    @Resource
    ProfileService profileService;

    @MockBean
    MessageUtil messageUtil;

    private UserProfile profile;

    @Value("${energyErrorMessage}")
    String ENERGY_ERROR_MESSAGE;

    @Value("${testProfileId}")
    Integer TEST_PROFILE_ID;

    @Value("${statusOk}")
    Integer STATUS_OK;

    @Value("${statusError}")
    Integer STATUS_ERROR;

    @Test
//    @Order(1)
//    @Sql(value = {"/prepare-user_profile.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void withdrawEnergyByStartGameTest() {
//        successLoginTest();
        profile = profileService.selectUserProfile(TEST_PROFILE_ID);

        assertSame(25, profile.getEnergy());
        assertSame(100, profile.getMoney());

        IntStream.rangeClosed(1, 5).forEach(i -> {
            var startGameResponse = request(new StartGameRequest(), StartGameResponse.class);
            assertSame(STATUS_OK, startGameResponse.errorCode);
        });
        var startGameResponse = request(new StartGameRequest(), StartGameResponse.class);
        assertSame(STATUS_ERROR, startGameResponse.errorCode);
    }

    // mock method
    public StartGameResponse request(StartGameRequest message, Class<StartGameResponse> responseClass) {
        if (profile.getEnergy() >= 5) {
            profile.setEnergy(profile.getEnergy() - 5);

            return new StartGameResponse();
        } else {
            var startGameResponse = new StartGameResponse();
            startGameResponse.errorCode = STATUS_ERROR;
            startGameResponse.errorMessage = ENERGY_ERROR_MESSAGE;

            return startGameResponse;
        }
    }

    @Test
//    @Order(2)
//    @Sql(value = {"/prepare-user_profile.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void withdrawEnergyByStartGameTestWithRealRequestShouldPassAndEnergyBeChangedInUserProfileDataBase() {
        successLoginTest();

        //WHEN
        StartGameResponse response = clientConnection.request(new StartGameRequest(), StartGameResponse.class);

        //THEN
        profile = profileService.selectUserProfile(TEST_PROFILE_ID);
        assertSame(20, profile.getEnergy());
        assertSame(STATUS_OK, response.errorCode);


        FinishGameRequest finishGameRequest = new FinishGameRequest();
        finishGameRequest.setResult(GameResult.WIN);
        clientConnection.request(finishGameRequest, FinishGameResponse.class);
    }

    @Test
//    @Order(3)
//    @Sql(value = {"/prepare-user_profile.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void withdrawEnergyByStartGameTestRequestShouldNotPassAndReturnErrorMessageAndEnergyEqualsZeroAndCodeError() {
//        successLoginTest();
        StartGameRequest startGameRequest = new StartGameRequest();

        //WHEN
        when((messageUtil).isRequestDuplicate(startGameRequest)).thenReturn(false);
        IntStream.rangeClosed(1, 5).forEach(i -> {
            var startGameResponse = clientConnection.request(new StartGameRequest(), StartGameResponse.class);
        });

        StartGameResponse response = clientConnection.request(startGameRequest, StartGameResponse.class);

        //THEN
        profile = profileService.selectUserProfile(TEST_PROFILE_ID);
        assertSame(STATUS_OK, profile.getEnergy());
        assertSame(STATUS_ERROR, response.errorCode);
        assertEquals(ENERGY_ERROR_MESSAGE, response.errorMessage);
    }


    @Test
//    @Order(4)
//    @Sql(value = {"/prepare-user_profile.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void recalculateUserLevelAndExperienceWithAwardTestShouldChangeUserExperienceLevelAndAward() {
//        successLoginTest();
        //GIVEN
        FinishGameRequest finishGameRequest = new FinishGameRequest();
        finishGameRequest.setResult(GameResult.WIN);

        //WHEN
        when((messageUtil).isRequestDuplicate(finishGameRequest)).thenReturn(false);
        IntStream.rangeClosed(1, 6).forEach(i -> {
            var startGameResponse = clientConnection.request(finishGameRequest, FinishGameResponse.class);
        });


        //THEN
        profile = profileService.selectUserProfile(TEST_PROFILE_ID);
        assertEquals(4, profile.getLevel());
        assertEquals(10, profile.getExperience());
        assertEquals(325, profile.getEnergy());
        assertEquals(18, profile.getRating());
        assertEquals(460, profile.getMoney());
    }

}
