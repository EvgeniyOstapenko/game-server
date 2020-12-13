package client;

import common.messages.FinishGameRequest;
import common.messages.FinishGameResponse;
import common.messages.StartGameRequest;
import common.messages.StartGameResponse;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import server.ServerApplication;
import server.common.GameResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;


@SpringBootTest(classes = ServerApplication.class)
//@TestPropertySource("/application-test.properties")
@TestPropertySource(locations = { "/application-test.properties", "/application.properties" })
public class FinishGameRequestTest extends ConnectAndLoginTests {

    @Value("${duplicateFinishRequestsErrorMessage}")
    String duplicateRequestsErrorMessage;

    @Value("${ok}")
    Integer ok;

    @Test
    @Order(1)
    public void start() throws Exception {
//        successLoginTest();

        //GIVEN
        FinishGameRequest finishGameRequest = new FinishGameRequest();
        finishGameRequest.setResult(GameResult.WIN);

        //WHEN
        FinishGameResponse response = clientConnection.request(finishGameRequest, FinishGameResponse.class);

        //THEN
        assertSame(ok, response.errorCode);

        //AFTER
        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
    }


    @Test
    @Order(2)
    public void onMessageTestWithDuplicateStartInTheRowGameRequestShouldReturnErrorResponse() {
//        successLoginTest();

        //GIVEN
        FinishGameRequest defeatGameFinishRequest = new FinishGameRequest();
        defeatGameFinishRequest.setResult(GameResult.DEFEAT);

        //WHEN
        FinishGameResponse response = clientConnection.request(defeatGameFinishRequest, FinishGameResponse.class);

        //THEN
        assertSame(1, response.errorCode);
        assertEquals("Not enough rating!", response.errorMessage);

        //AFTER
        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
    }


    @Test
    @Order(3)
    public void onMessageTestWithDuplicateStartInTheRowGameRequestShouldReturnErrorResponse2() {
        successLoginTest();

        //GIVEN
        FinishGameRequest defeatGameFinishRequest = new FinishGameRequest();
        defeatGameFinishRequest.setResult(GameResult.DEFEAT);

        //WHEN
        clientConnection.request(defeatGameFinishRequest, FinishGameResponse.class);
        FinishGameResponse response = clientConnection.request(defeatGameFinishRequest, FinishGameResponse.class);

        //THEN
        assertSame(2, response.errorCode);
        assertEquals(duplicateRequestsErrorMessage, response.errorMessage);

        //AFTER
        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
    }
}
