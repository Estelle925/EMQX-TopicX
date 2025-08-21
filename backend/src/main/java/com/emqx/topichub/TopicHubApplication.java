package com.emqx.topichub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EMQX Topic管理增强服务启动类
 * 
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@SpringBootApplication
public class TopicHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(TopicHubApplication.class, args);
    }

}