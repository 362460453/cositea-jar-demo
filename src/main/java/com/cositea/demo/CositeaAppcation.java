package com.cositea.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


/**
 * 
 * TODO:启动类
 * 
 * @author liuyicong
 * @Date 2018年11月26日 上午9:48:15
 */
@SpringBootApplication //Spring Boot核心注解，用于开启自动配置
@EnableScheduling
public class CositeaAppcation extends WebMvcConfigurerAdapter{
    
    public static void main( String[] args ) {
        SpringApplication.run(CositeaAppcation.class, args);
    }
}
