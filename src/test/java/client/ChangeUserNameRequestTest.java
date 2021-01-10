package client;

import common.messages.ChangeUserNameRequest;
import common.messages.ChangeUserNameResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import server.ServerApplication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@SpringBootTest(classes = ServerApplication.class)
@TestPropertySource(locations = {"/application-test.properties"})
public class ChangeUserNameRequestTest extends ConnectAndLoginTests {

    @Value("${statusOk}")
    Integer STATUS_OK;

    @Value("${statusError}")
    Integer STATUS_ERROR;

    @Value("${changeNameErrorMassage}")
    String CHANGE_NAME_ERROR_MESSAGE;

    @Test
    public void changeUserNameRequestTestSentSingleRequestShouldPassWithoutError() throws Exception {
        successLoginTest();

        //GIVEN
        ChangeUserNameRequest changeUserNameRequest = new ChangeUserNameRequest();
        changeUserNameRequest.setNewUserName("newName");

        //WHEN
        ChangeUserNameResponse response = clientConnection.request(changeUserNameRequest, ChangeUserNameResponse.class);

        //THEN
        assertSame(STATUS_OK, response.errorCode);
    }

    @Test
    public void changeUserNameRequestTestSentDuplicateRequestInTheSameDayShouldNotPassAndReturnError() throws Exception {
//        successLoginTest();

        //GIVEN
        ChangeUserNameRequest changeUserNameRequest = new ChangeUserNameRequest();
        changeUserNameRequest.setNewUserName("newName");

        //WHEN
        clientConnection.request(changeUserNameRequest, ChangeUserNameResponse.class);
        ChangeUserNameResponse response = clientConnection.request(changeUserNameRequest, ChangeUserNameResponse.class);

        //THEN
        assertSame(STATUS_ERROR, response.errorCode);
        assertEquals(CHANGE_NAME_ERROR_MESSAGE, response.errorMessage);
    }
}
