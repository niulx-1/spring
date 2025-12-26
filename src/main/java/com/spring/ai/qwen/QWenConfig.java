package com.spring.ai.qwen;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class QWenConfig implements ApplicationContextAware {

    @Value("${spring.ai.qwen.api-key:sk-a08b4a72f68f48239fa44d8901320bbc}")
    private String apiKey;

    @Value("${spring.ai.qwen.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;

    @Value("${spring.ai.qwen.model:qwen3-max}")
    private String model;


    private static ApplicationContext applicationContext;


    @Bean
    public RestClient qwenRestClient() {
        return RestClient.builder().baseUrl(baseUrl).defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey).defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder().baseUrl(baseUrl).defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey).defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
    }

    @Override
    public void setApplicationContext(ApplicationContext _applicationContext) throws BeansException {
        if (null == applicationContext) {
            QWenConfig.applicationContext = _applicationContext;
        }
    }


    public static String getApiKey() {
        QWenConfig qWenConfig = applicationContext.getBean(QWenConfig.class);
        return qWenConfig.apiKey;
    }

    public static String getBaseUrl() {
        QWenConfig qWenConfig = applicationContext.getBean(QWenConfig.class);
        return qWenConfig.baseUrl;
    }

    public static String getModel() {
        QWenConfig qWenConfig = applicationContext.getBean(QWenConfig.class);
        return qWenConfig.model;
    }


}