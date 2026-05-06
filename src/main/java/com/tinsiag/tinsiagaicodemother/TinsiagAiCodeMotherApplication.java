package com.tinsiag.tinsiagaicodemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.tinsiag.tinsiagaicodemother.mapper")
@SpringBootApplication
public class TinsiagAiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(TinsiagAiCodeMotherApplication.class, args);
    }

}
