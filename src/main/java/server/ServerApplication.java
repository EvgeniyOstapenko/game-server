package server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import server.domain.TopItem;
import server.service.TopService;

import java.util.List;
import java.util.Map;

@SpringBootApplication(scanBasePackages = {"platform", "server", "common"})
@ImportResource({"classpath:beans.xml"})
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);

//        TopService topService = new TopService();
//        List<Map<String, Object>> topList = topService.get();
//        System.out.println(topList);
    }
}
