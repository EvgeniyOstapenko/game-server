package client;

import common.exception.DuplicateMessageStateException;
import common.messages.StartGameRequest;
import common.messages.StartGameResponse;
//import org.junit.Test;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import server.ServerApplication;

@SpringBootTest(classes = ServerApplication.class)
public class StartGameRequestTest extends ConnectAndLoginTests {

    @Test
    public void start() throws Exception {
        successLoginTest();

        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
    }


    @Test
    public void startGameRequestTestWithDuplicateStartInTheRowGameRequestShouldReturnErrorMessage() {
        successLoginTest();

        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
        clientConnection.request(new StartGameRequest(), StartGameResponse.class);

//        Mockito.verify(clientConnection.request(new StartGameRequest(), StartGameResponse.class).errorMessage).
    }
}
