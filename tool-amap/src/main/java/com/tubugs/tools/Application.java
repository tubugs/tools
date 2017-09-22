package com.tubugs.tools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by xuzhang on 2017/8/26.
 */
@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private GrabAmap grabAmap;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        application.run(args);
    }

    @Override
    public void run(String... strings) throws Exception {
        grabAmap.start();
    }
}
