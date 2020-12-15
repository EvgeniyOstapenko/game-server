package client;

import common.messages.*;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import server.ServerApplication;
import server.common.GameResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@SpringBootTest(classes = ServerApplication.class)
@TestPropertySource("/application-test.properties")
public class TopRequestTest  extends ConnectAndLoginTests {

    @Value("${statusOk}")
    Integer STATUS_OK;

    @Value("${statusError}")
    Integer STATUS_ERROR;

    @Test
    public void start() throws Exception {
        successLoginTest();

        //WHEN
        TopResponse response = clientConnection.request(new TopRequest(), TopResponse.class);

        //THEN
        assertSame(STATUS_OK, response.errorCode);

    }

    @Test
    @Sql(value = {"/prepare-user_profile.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void onMessageTestRequestToGetTopUserListShouldReturnTopUsersList() {
        successLoginTest();

        //WHEN
        TopResponse response = clientConnection.request(new TopRequest(), TopResponse.class);

        //THEN
        assertSame(STATUS_OK, response.errorCode);
//        assertEquals(RATING_ERROR_MESSAGE, response.errorMessage);

    }


}