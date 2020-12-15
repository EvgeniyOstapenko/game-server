package client;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import server.ServerApplication;

@SpringBootTest(classes = ServerApplication.class)
@TestPropertySource(locations = { "/application-test.properties" })
public class ChangeUserNameRequestTest extends ConnectAndLoginTests{
}
