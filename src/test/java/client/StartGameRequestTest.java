package client;

import common.exception.DuplicateMessageStateException;
import common.messages.*;
//import org.junit.Test;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import server.ServerApplication;

@SpringBootTest(classes = ServerApplication.class)
public class StartGameRequestTest extends ConnectAndLoginTests {

    @Test
    public void start() throws Exception {

        clientConnection.request(new StartGameRequest(), ErrorResponse.class);
    }


    @Test
    public void startGameRequestTestWithDuplicateStartInTheRowGameRequestShouldReturnErrorResponse() {

        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
        clientConnection.request(new StartGameRequest(), ErrorResponse.class);
        clientConnection.request(new FinishGameRequest(), FinishGameResponse.class);
    }
}
