package com.jlwl;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@MapperScan({"com.jlwl.**.dao", "com.jlwl.**.mapper"})
public class OaApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(OaApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  OA System V2 Started Successfully");
        System.out.println("  URL: http://localhost:8080/springboot-oa-v2");
        System.out.println("  Admin Login: admin/admin");
        System.out.println("========================================\n");
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder) {
        return applicationBuilder.sources(OaApplication.class);
    }
}
