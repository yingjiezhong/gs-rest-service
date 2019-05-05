package com.tutorial;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import javax.annotation.PostConstruct;

@SpringBootApplication

//@Configuration

// component scan can be replaced my manual import
//@ComponentScan
//@Import({ArticleController.class, JobControl.class})
//@Import({ArticleController.class,GreetingController.class, JobControl.class})

//@EnableAutoConfiguration
@ImportResource("classpath:app-config.xml")
public class Application {

    @Autowired
    Jackson2ObjectMapperFactoryBean jackson2ObjectMapper;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    private void init() {
        System.out.println("InitDemoApplication initialization logic ...");
    }

    @Bean
    public MappingJackson2HttpMessageConverter messageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(jackson2ObjectMapper.getObject());
        System.out.println("created bean ...... ");
        return converter;
    }
}
