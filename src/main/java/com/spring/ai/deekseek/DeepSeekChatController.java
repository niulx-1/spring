package com.spring.ai.deekseek;

import com.spring.ai.advisors.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/deepseek/ai")
@RestController
public class DeepSeekChatController {

    @Autowired
    ObjectFactory<ChatClient.Builder> builderFactory;


    public ChatClient createClient() {
        ChatClient.Builder builder = builderFactory.getObject();
        ChatOptions options = ChatOptions.builder().model(DeepSeekApi.ChatModel.DEEPSEEK_CHAT.getValue()).build();
        builder.defaultOptions(options);
        builder.defaultAdvisors(new SimpleLoggerAdvisor());
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
