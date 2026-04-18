package org.zeroagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.logging.LogManager;

@SpringBootApplication
public class ZeroAgentApplication {

    public static void main(String[] args) {
        // jul -> log4j2
        System.setProperty("java.util.logging.manager", LogManager.class.getName());
        SpringApplication.run(ZeroAgentApplication.class, args);
    }

}
