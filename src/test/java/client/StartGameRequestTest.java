package client;

import common.messages.StartGameRequest;
import common.messages.StartGameResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import server.ServerApplication;

@SpringBootTest(classes = ServerApplication.class)
public class StartGameRequestTest extends ConnectAndLoginTests {

    @Test
    public void StartGameRequestTest() {
        successLoginTest();

        clientConnection.request(new StartGameRequest(), StartGameResponse.class);
    }
}
