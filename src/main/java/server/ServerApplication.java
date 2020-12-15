package server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import server.domain.TopItem;
import server.service.TopService;
import server.service.UserProfileDao;

import java.util.List;
import java.util.Map;

@SpringBootApplication(scanBasePackages = {"platform", "server", "common"})
@ImportResource({"classpath:beans.xml"})
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

}
