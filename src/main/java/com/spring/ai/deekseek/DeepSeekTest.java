package com.spring.ai.deekseek;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RequestMapping("/deepseek/test")
@RestController
public class DeepSeekTest {

    public static OkHttpClient client = new OkHttpClient().newBuilder().build();

    @GetMapping("/chat")
    public String chat(String userInput) throws IOException {
        MediaType mediaType = MediaType.parse("application/json");

        DeepSeekRequest.Message message1 = new DeepSeekRequest.Message("system", "You are a helpful assistant");
        DeepSeekRequest.Message message2 = new DeepSeekRequest.Message("user", StringUtils.isBlank(userInput) ? "介绍下自己" : userInput);
        DeepSeekRequest.Thinking thinking = new DeepSeekRequest.Thinking("disabled");
        DeepSeekRequest deepSeekRequest = new DeepSeekRequest(List.of(message1, message2), DeepSeekConfig.getModel(), thinking);

        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(deepSeekRequest);

        RequestBody body = RequestBody.create(mediaType, json);
        Request request = new Request.Builder()
                .url(DeepSeekConfig.getBaseUrl() + "/chat/completions")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + DeepSeekConfig.getApiKey())
                .build();
        Response response = client.newCall(request).execute();
        assert response.body() != null;
        return response.body().string();
    }


    public record DeepSeekRequest(List<Message> messages, String model, Thinking thinking) {
        public record Message(String role, String content) {
        }

        public record Thinking(String type) {
        }
    }


    @GetMapping("/models")
    public String models() throws IOException {
        Request request = new Request.Builder()
                .url(DeepSeekConfig.getBaseUrl() + "/models")
                .method("GET", null)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + DeepSeekConfig.getApiKey())
                .build();
        Response response = client.newCall(request).execute();
        assert response.body() != null;
        return response.body().string();
    }


    @GetMapping("/balance")
    public String balance() throws IOException {
        Request request = new Request.Builder()
                .url(DeepSeekConfig.getBaseUrl() + "/user/balance")
                .method("GET", null)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + DeepSeekConfig.getApiKey())
                .build();
        Response response = client.newCall(request).execute();
        assert response.body() != null;
        return response.body().string();
    }
}
