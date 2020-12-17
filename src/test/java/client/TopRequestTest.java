package client;

import common.messages.TopRequest;
import common.messages.TopResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import server.ServerApplication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@SpringBootTest(classes = ServerApplication.class)
@TestPropertySource("/application-test.properties")
public class TopRequestTest extends ConnectAndLoginTests {

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

    @Test
    @Sql(value = {"/toFill-user_profile.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/toClean-user_profile.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void onMessageTestRequestToGetTopUserListShouldReturnTopUsersList() {
//        successLoginTest();

        //WHEN
        TopResponse response = clientConnection.request(new TopRequest(), TopResponse.class);

        //THEN
        assertSame(STATUS_OK, response.errorCode);
        assertEquals(NUMBER_OF_PLAYERS, response.topList.size());
    }


}