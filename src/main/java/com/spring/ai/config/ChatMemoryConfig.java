package com.spring.ai.config;

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

    @Bean
    public MessageWindowChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().build();
    }

    @Bean
    public MessageChatMemoryAdvisor messageChatMemoryAdvisor(
            MessageWindowChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }
}