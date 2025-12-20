package com.spring.ai.openai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/ai")
public class OpenChatController {

    @Autowired
    ObjectFactory<ChatClient.Builder> builderFactory;


    public ChatClient createClient() {
        ChatClient.Builder builder = builderFactory.getObject();
        builder.defaultAdvisors();
        return builder.build();
    }

    @GetMapping("/ai")
    String generation(String userInput) {
        return createClient().prompt()
                .user(userInput)
                .call()
                .content();
    }

}
