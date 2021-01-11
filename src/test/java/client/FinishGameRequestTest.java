package client;

import common.messages.FinishGameRequest;
import common.messages.FinishGameResponse;
import common.messages.StartGameRequest;
import common.messages.StartGameResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import server.ServerApplication;
import server.common.GameResult;
import server.domain.UserProfile;
import server.service.ProfileService;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;


@SpringBootTest(classes = ServerApplication.class)
@TestPropertySource(locations = {"/application-test.properties"})
public class FinishGameRequestTest extends ConnectAndLoginTests {

    @Resource
    ProfileService profileService;

    @Value("${duplicateFinishRequestsErrorMessage}")
    String DUPLICATE_REQUEST_ERROR_MESSAGE;

    @Value("${statusOk}")
    Integer STATUS_OK;

    @Value("${statusError}")
    Integer STATUS_ERROR;

    @Value("${testProfileId}")
    Integer TEST_PROFILE_ID;


    @Test
    public void finishGameRequestTest() throws Exception {
        successLoginTest();

        //GIVEN
        FinishGameRequest finishGameRequest = new FinishGameRequest();
        finishGameRequest.setResult(GameResult.WIN);

        //WHEN
        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
        FinishGameResponse response = clientConnection.request(finishGameRequest, FinishGameResponse.class);

        //THEN
        assertSame(STATUS_OK, response.errorCode);
    }


    @Test
    public void onMessageTestWithDuplicateStartInTheRowGameRequestShouldReturnErrorResponse() {
//        successLoginTest();

        //GIVEN
        FinishGameRequest defeatGameFinishRequest = new FinishGameRequest();
        defeatGameFinishRequest.setResult(GameResult.DEFEAT);

        //WHEN
        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
        clientConnection.request(defeatGameFinishRequest, FinishGameResponse.class);
        FinishGameResponse response = clientConnection.request(defeatGameFinishRequest, FinishGameResponse.class);

        //THEN
        assertSame(STATUS_ERROR, response.errorCode);
        assertEquals(DUPLICATE_REQUEST_ERROR_MESSAGE, response.errorMessage);
    }

    @Test
    @Sql(value = {"/prepare-user_profile.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void onMessageTestForUserAwardReturnShouldReturnResponseWithUserAward() {
//        successLoginTest();

        //GIVEN
        FinishGameRequest defeatGameFinishRequest = new FinishGameRequest();
        defeatGameFinishRequest.setResult(GameResult.WIN);

        //WHEN
        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
        FinishGameResponse response = clientConnection.request(defeatGameFinishRequest, FinishGameResponse.class);

        //THEN
        assertSame(STATUS_OK, response.errorCode);
        assertSame(20, response.award.getEnergy());

        //AFTER
        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
    }

    @Test
    @Sql(value = {"/prepare-user_profile.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void onMessageTestForCheckingRatingAlwaysPositiveReturnShouldReturnResponseWithUserZeroRating() {
//        successLoginTest();

        //GIVEN
        FinishGameRequest defeatGameFinishRequest = new FinishGameRequest();
        defeatGameFinishRequest.setResult(GameResult.DEFEAT);

        //WHEN
        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
        FinishGameResponse response = clientConnection.request(defeatGameFinishRequest, FinishGameResponse.class);

        //THEN
        assertSame(STATUS_OK, response.errorCode);
        UserProfile profile = profileService.selectUserProfile(TEST_PROFILE_ID);
        assertSame(0, profile.getRating());

        //AFTER
        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
    }


    @Test
    @Sql(value = {"/prepare-user_profile.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void onMessageTestForUserWithFinalLevelShouldReturnResponseWithTheSameFinalUserLevel() {
        successLoginTest();

        //GIVEN
        FinishGameRequest victoryRequest = new FinishGameRequest();
        victoryRequest.setResult(GameResult.WIN);

        //WHEN
        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
        FinishGameResponse response = clientConnection.request(victoryRequest, FinishGameResponse.class);

        //THEN
        assertSame(STATUS_OK, response.errorCode);
        assertSame(20, response.award.getEnergy());

        //AFTER
        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
    }
}
