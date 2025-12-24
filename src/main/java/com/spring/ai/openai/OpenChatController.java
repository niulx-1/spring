package com.spring.ai.openai;

import com.spring.ai.advisors.SimpleLoggerAdvisor;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/open/ai")
@RestController
public class OpenChatController {

    @Autowired
    ObjectFactory<ChatClient.Builder> builderFactory;

    @Resource
    SimpleLoggerAdvisor simpleLoggerAdvisor;


    public ChatClient createClient() {
        ChatClient.Builder builder = builderFactory.getObject();
        builder.defaultAdvisors(simpleLoggerAdvisor);
        return builder.build();
    }

    @GetMapping("/chat")
    String generation(String userInput) {
        return createClient().prompt()
                .user(userInput)
                .call()
                .content();
    }


    @GetMapping("/chat-v1")
    String generation_v1(String userInput) {
        var chatResponse = createClient().prompt()
                .user(userInput)
                .call()
                .chatResponse();
        assert chatResponse != null;
        return chatResponse.getResult().getOutput().getText();
    }

}
