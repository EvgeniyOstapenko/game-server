package client;

import common.messages.StartGameRequest;
import common.messages.StartGameResponse;
import org.junit.jupiter.api.Order;
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

    @Value("${duplicateStartRequestsErrorMessage}")
    String DUPLICATE_REQUEST_ERROR_MESSAGE;

    @Value("${statusOk}")
    Integer STATUS_OK;

    @Value("${statusError}")
    Integer STATUS_ERROR;

    @Test
    @Order(1)
    public void start() throws Exception {
        successLoginTest();

        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
    }


    @Test
    @Order(2)
    public void onMessageTestWithDuplicateStartInTheRowGameRequestShouldReturnErrorResponse() {
//        successLoginTest();

        //WHEN
        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
        StartGameResponse response = clientConnection.request(new StartGameRequest(), StartGameResponse.class);

        //THEN
        assertSame(STATUS_ERROR, response.errorCode);
        assertEquals(DUPLICATE_REQUEST_ERROR_MESSAGE, response.errorMessage);

        //AFTER
//        clientConnection.request(new FinishGameRequest(), FinishGameResponse.class);
    }
}
