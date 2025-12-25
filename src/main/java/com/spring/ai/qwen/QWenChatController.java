package com.spring.ai.qwen;

import com.alibaba.dashscope.aigc.generation.SearchOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
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

    @Qualifier("qwenChatModel")
    @Autowired
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
                Collections.singletonMap("text", "请问图片展现了什么商品?"));

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(list);

        return chatModel.call(json);
    }


    @GetMapping("/getImageInfo")
    String getImageInfo(String userInput) throws JsonProcessingException {
        Map<String, Object> map = Maps.newHashMap();
        map.put("type", "text");
        map.put("text", "请提取车票图像中的发票号码、车次、起始站、终点站、发车日期和时间点、座位号、席别类型、票价、身份证号码、购票人姓名。要求准确无误的提取上述关键信息、不要遗漏和捏造虚假信息，模糊或者强光遮挡的单个文字可以用英文问号?代替。返回数据格式以json方式输出，格式为：{'发票号码': 'xxx'");
        Map<String, Object> map1 = Maps.newHashMap();
        map1.put("type", "image_url");
        map1.put("image_url", "https://img.alicdn.com/imgextra/i2/O1CN01ktT8451iQutqReELT_!!6000000004408-0-tps-689-487.jpg");
        map1.put("min_pixels", 3072);
        map1.put("max_pixels", 8388608);
        List<Map<String, Object>> list = Arrays.asList(map1, map);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(list);
        return chatModel.call(json);
    }

}
