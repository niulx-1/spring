package com.spring.ai.qwen;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/qwen/ai")
@RestController
public class QWenChatController {

    @Qualifier("qwenChatModel")
    @Autowired
    ChatModel chatModel;

    @GetMapping("/chat")
    String generation(String userInput) {
        return chatModel.call(userInput);
    }

}
