package server;

import common.messages.AbstractResponse;
import common.messages.FinishGameResponse;
import common.messages.StartGameResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication(scanBasePackages = {"platform", "server", "common"})
@ImportResource({"classpath:beans.xml"})
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);

    }
}
