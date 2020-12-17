package client;

import client.connection.ClientConnection;
import common.messages.EnterAccount;
import common.messages.LoginError;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ConnectAndLoginTests {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected static final String VALID_TOKEN = "ValidToken";

    protected static ClientConnection clientConnection;

    protected EnterAccount enterAccount;

    protected static String uid = UUID.randomUUID().toString();

    protected void connect() {
        clientConnection = new ClientConnection("localhost", 5000).connect();
    }

    @Test
    public void successLoginTest() {
        connect();
        enterAccount = clientConnection.request(new common.messages.Login(uid, VALID_TOKEN), EnterAccount.class);
    }

    @Test
    public void reLoginTest() throws InterruptedException {
        if (clientConnection != null && clientConnection.isActive()) {
            clientConnection.disconnect();
        }

        successLoginTest();

        ClientConnection prevConnection = clientConnection;
        connect();
        clientConnection.request(new common.messages.Login(uid, VALID_TOKEN), LoginError.class);

        Thread.sleep(300);
        Assertions.assertFalse(prevConnection.isActive());
    }

    public <RESPONSE> RESPONSE request(Object message, Class<RESPONSE> responseClass) {
        return clientConnection.request(message, responseClass);
    }

    @AfterAll
    public static void destroy() {
        clientConnection.disconnect();
    }

}
