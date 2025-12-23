package com.spring.ai.deekseek;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DeepSeekConfig implements ApplicationContextAware {

    @Value("${spring.ai.deepseek.api-key:sk-68fc86d2613a47dd9bac1651ac9e6718}")
    private String apiKey;

    @Value("${spring.ai.deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${spring.ai.qwen.model:deepseek-chat}")
    private String model;


    private static ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext _applicationContext) throws BeansException {
        if (null == applicationContext) {
            DeepSeekConfig.applicationContext = _applicationContext;
        }
    }


    public static String getApiKey() {
        DeepSeekConfig config = applicationContext.getBean(DeepSeekConfig.class);
        return config.apiKey;
    }

    public static String getBaseUrl() {
        DeepSeekConfig config = applicationContext.getBean(DeepSeekConfig.class);
        return config.baseUrl;
    }

    public static String getModel() {
        DeepSeekConfig config = applicationContext.getBean(DeepSeekConfig.class);
        return config.model;
    }


}