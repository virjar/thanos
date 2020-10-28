package com.virjar.thanos;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(exclude = {
        // RedisRepositoriesAutoConfiguration.class,
        MongoAutoConfiguration.class
})
@EnableSwagger2
@MapperScan("com.virjar.thanos.mapper")
public class ThanosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThanosApplication.class, args);
    }

}
