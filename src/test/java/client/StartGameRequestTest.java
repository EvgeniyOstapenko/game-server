package client;

import common.messages.FinishGameRequest;
import common.messages.FinishGameResponse;
import common.messages.StartGameRequest;
import common.messages.StartGameResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import server.ServerApplication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@SpringBootTest(classes = ServerApplication.class)
@TestPropertySource("/application-test.properties")
public class StartGameRequestTest extends ConnectAndLoginTests {

    @Value("${StartGameRequestErrorMessage}")
    String errorMessage;

    @Test
    public void start() throws Exception {
        successLoginTest();

        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
    }


    @Test
    public void onMessageTestWithDuplicateStartInTheRowGameRequestShouldReturnErrorResponse() {
//        successLoginTest();

        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
        StartGameResponse response = clientConnection.request(new StartGameRequest(), StartGameResponse.class);
        assertSame(1, response.errorCode);
        assertEquals(errorMessage, response.errorMessage);

        clientConnection.request(new FinishGameRequest(), FinishGameResponse.class);
    }
}
