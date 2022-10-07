package com.entingwu.ecommerceseckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.entingwu.ecommerceseckill.db.mappers")
@ComponentScan(basePackages = {"com.entingwu"})
public class EcommerceSecKillApplication {

    public static void main(String[] args) {

        SpringApplication.run(EcommerceSecKillApplication.class, args);
    }

}
