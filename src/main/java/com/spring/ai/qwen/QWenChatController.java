package com.spring.ai.qwen;

import com.alibaba.dashscope.aigc.generation.SearchOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/qwen/ai")
@RestController
public class QWenChatController {

//    @Qualifier("qwenChatModel")
//    @Autowired
    ChatModel chatModel;

    @GetMapping("/chat")
    String generation(String userInput) {
        return chatModel.call(userInput);
    }

    @GetMapping("/search")
    String search(String userInput) {
        Map<String, Object> maps = new HashMap<>();
        maps.put("enable_search", true);
        SearchOptions options = SearchOptions.builder().searchStrategy("max").enableSource(true).build();
        maps.put("search_options", options);
        UserMessage message = UserMessage.builder().text(userInput).metadata(maps).build();
        return chatModel.call(message);
    }

    @GetMapping("/image")
    String image(String url) throws Exception {

        List<Map<String, String>> list = Arrays.asList(Collections.singletonMap("image", StringUtils.isEmpty(url) ? "https://help-static-aliyun-doc.aliyuncs.com/file-manage-files/zh-CN/20251031/ownrof/f26d201b1e3f4e62ab4a1fc82dd5c9bb.png" : url),
                Collections.singletonMap("text", "请问图片展现了什么东西?"));

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(list);

        return chatModel.call(json);
    }

}
