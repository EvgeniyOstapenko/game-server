package client;

import common.messages.*;
import common.util.MessageUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import server.ServerApplication;
import server.common.GameResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ServerApplication.class)
@TestPropertySource("/application-test.properties")
public class TopRequestTest extends ConnectAndLoginTests {

    @MockBean
    MessageUtil messageUtil;

    @Value("${statusOk}")
    Integer STATUS_OK;

    @Value("${statusError}")
    Integer STATUS_ERROR;

    @Value("${numberOfTopPlayers}")
    Integer NUMBER_OF_PLAYERS;

    @Test
    public void topRequestTest() throws Exception {
        successLoginTest();

        //WHEN
        TopResponse response = clientConnection.request(new TopRequest(), TopResponse.class);

        //THEN
        assertSame(STATUS_OK, response.errorCode);

    }

//    @Test
    @Sql(value = {"/toFill-user_profile.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/toClean-user_profile.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void onMessageTestRequestToGetTopUserListShouldReturnTopUsersList1() {
//        successLoginTest();

        //WHEN
        TopResponse response = clientConnection.request(new TopRequest(), TopResponse.class);

        //THEN
        assertSame(STATUS_OK, response.errorCode);
        assertEquals(NUMBER_OF_PLAYERS, response.topList.size());
    }

    @Test
    public void onMessageTestRequestToGetTopUserListShouldReturnTopUsersList2() {
        successLoginTest();

        //GIVEN
        FinishGameRequest finishGameRequest = new FinishGameRequest();
        finishGameRequest.setResult(GameResult.WIN);
        StartGameRequest startGameRequest = new StartGameRequest();

        //WHEN
        when((messageUtil).isRequestDuplicate(startGameRequest)).thenReturn(false);
        clientConnection.request(startGameRequest, StartGameResponse.class);
        clientConnection.request(startGameRequest, StartGameResponse.class);

        enterAccount = clientConnection.request(new common.messages.Login(UUID.randomUUID().toString(), VALID_TOKEN), EnterAccount.class);
        clientConnection.request(startGameRequest, StartGameResponse.class);
        clientConnection.request(startGameRequest, StartGameResponse.class);

        clientConnection.request(finishGameRequest, FinishGameResponse.class);
        TopResponse response = clientConnection.request(new TopRequest(), TopResponse.class);

        //THEN
        assertSame(STATUS_OK, response.errorCode);
//        assertEquals(NUMBER_OF_PLAYERS, response.topList.size());
    }


}