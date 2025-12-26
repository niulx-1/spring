package com.spring.ai.qwen;


import com.alibaba.dashscope.aigc.completion.ChatCompletionStreamOptions;
import com.alibaba.dashscope.aigc.generation.SearchOptions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Configuration(value = "qwenChatModel")
public class QWenChatModel implements ChatModel {

    @Resource
    RestClient restClient;

    @Resource
    WebClient webClient;

    Predicate<String> SSE_DONE_PREDICATE = "[DONE]"::equals;

    // todo qwen api 目前不兼容spring chatmodel 接口，需要自己实现， 把 chatoptions 全部放到 metadata 中， 兼容 spring chatmodel 接口

    @Override
    public ChatResponse call(Prompt prompt) {
        Map<String, Object> requestBody = Maps.newHashMap();
        requestBody.put("messages", convertMessages(prompt.getUserMessages()));
        requestBody.put("model", QWenConfig.getModel());

        List<UserMessage> userMessages = prompt.getUserMessages();

        Map<String, Object> metadata = userMessages.get(0).getMetadata();

        if (metadata.containsKey("enable_search")) {
            requestBody.put("enable_search", metadata.get("enable_search"));
            if (metadata.containsKey("search_options")) {
                SearchOptions searchOptions = (SearchOptions) metadata.get("search_options");
                requestBody.put("search_options", searchOptions);
            }
        }

        ResponseEntity<ChatCompletion> response = restClient.post().uri("/chat/completions").body(requestBody).retrieve().toEntity(ChatCompletion.class);

        var chatCompletion = response.getBody();

        if (chatCompletion == null) {
            log.warn("No chat completion returned for prompt: {}", prompt);
            return new ChatResponse(List.of());
        }

        List<ChatCompletion.Choice> choices = chatCompletion.choices();
        if (choices == null) {
            log.warn("No choices returned for prompt: {}", prompt);
            return new ChatResponse(List.of());
        }

        List<Generation> generations = choices.stream().map(choice -> {
            // @formatter:off
            Map<String, Object> metadata1 = Map.of(
                    "id", chatCompletion.id() != null ? chatCompletion.id() : "",
                    "role", choice.message().role() != null ? choice.message().role().name() : "",
                    "index", choice.index(),
                    "finishReason", choice.finishReason() != null ? choice.finishReason() : "");
            // @formatter:on
            return buildGeneration(choice, metadata1);
        }).toList();

        return new ChatResponse(generations);
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        Map<String, Object> requestBody = Maps.newHashMap();
        requestBody.put("messages", convertMessages(prompt.getUserMessages()));
        requestBody.put("model", QWenConfig.getModel());

        List<UserMessage> userMessages = prompt.getUserMessages();

        Map<String, Object> metadata = userMessages.get(0).getMetadata();

        if (metadata.containsKey("enable_search")) {
            requestBody.put("enable_search", metadata.get("enable_search"));
            if (metadata.containsKey("search_options")) {
                SearchOptions searchOptions = (SearchOptions) metadata.get("search_options");
                requestBody.put("search_options", searchOptions);
            }
        }

        if (metadata.containsKey("stream")) {
            requestBody.put("stream", metadata.get("stream"));

            if (metadata.containsKey("stream_options")) {
                ChatCompletionStreamOptions streamOptions = (ChatCompletionStreamOptions) metadata.get("stream_options");
                Map<String, Object> streamOptionsMap = new HashMap<>();
                streamOptionsMap.put("include_usage", streamOptions.getIncludeUsage());

                requestBody.put("stream_options", streamOptionsMap);
            }
            if (metadata.containsKey("enable_thinking")) {
                requestBody.put("enable_thinking", metadata.get("enable_thinking"));
            }
        }

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .takeUntil(SSE_DONE_PREDICATE)
                .filter(SSE_DONE_PREDICATE.negate())
                .map(content -> ModelOptionsUtils.jsonToObject(content, Map.class))
                .map(data -> createChatResponseFromData((Map<String, Object>) data, prompt))
                .onErrorResume(e -> {
                    log.error("Streaming error", e);
                    return Flux.error(e);
                });
    }


    private ChatResponse createChatResponseFromData(Map<String, Object> data, Prompt prompt) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) data.get("choices");
        if (choices == null || choices.isEmpty()) {
            return new ChatResponse(Lists.newArrayList(new Generation(null)));
        }

        Map<String, Object> choice = choices.get(0);
        Map<String, Object> delta = (Map<String, Object>) choice.get("delta");

        if (delta == null) {
            return new ChatResponse(Lists.newArrayList(new Generation(null)));
        }

        String content = (String) delta.get("content");
        if (content == null || content.isEmpty()) {
            return new ChatResponse(Lists.newArrayList(new Generation(null)));
        }

        AssistantMessage message = new AssistantMessage(content);
        ChatResponseMetadata metadata = new ChatResponseMetadata();

        Generation generation = new Generation(message);
        return new ChatResponse(List.of(generation), metadata);
    }

    private Generation buildGeneration(ChatCompletion.Choice choice, Map<String, Object> metadata) {

        String finishReason = (choice.finishReason() != null ? choice.finishReason() : "");
        var generationMetadataBuilder = ChatGenerationMetadata.builder().finishReason(finishReason);

        String textContent = choice.message().rawContent().toString();

        AssistantMessage message = AssistantMessage.builder().content(textContent).properties(metadata).media(new ArrayList<>()).toolCalls(new ArrayList<>()).build();
        return new Generation(message, generationMetadataBuilder.build());
    }

    private List<Map<String, String>> convertMessages(List<UserMessage> messages) {
        return messages.stream().map(msg -> Map.of("role", convertRole(msg.getMessageType()), "content", msg.getText())).collect(Collectors.toList());
    }

    private String convertRole(MessageType messageType) {
        switch (messageType) {
            case USER:
                return "user";
            case ASSISTANT:
                return "assistant";
            case SYSTEM:
                return "system";
            default:
                return "user";
        }
    }


    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatCompletion(// @formatter:off
                                 @JsonProperty("id") String id,
                                 @JsonProperty("choices") List<ChatCompletion.Choice> choices,
                                 @JsonProperty("created") Long created,
                                 @JsonProperty("model") String model,
                                 @JsonProperty("system_fingerprint") String systemFingerprint,
                                 @JsonProperty("object") String object,
                                 @JsonProperty("usage") Usage usage
    ) { // @formatter:on

        /**
         * Chat completion choice.
         *
         * @param finishReason The reason the model stopped generating tokens.
         * @param index        The index of the choice in the list of choices.
         * @param message      A chat completion message generated by the model.
         * @param logprobs     Log probability information for the choice.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Choice(// @formatter:off
                             @JsonProperty("finish_reason") String finishReason,
                             @JsonProperty("index") Integer index,
                             @JsonProperty("message") ChatCompletionMessage message,
                             @JsonProperty("logprobs") String logprobs) { // @formatter:on
        }

    }


    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Usage(// @formatter:off
                        @JsonProperty("completion_tokens") Integer completionTokens,
                        @JsonProperty("prompt_tokens") Integer promptTokens,
                        @JsonProperty("total_tokens") Integer totalTokens,
                        @JsonProperty("prompt_tokens_details") Usage.PromptTokensDetails promptTokensDetails) { // @formatter:on

        public Usage(Integer completionTokens, Integer promptTokens, Integer totalTokens) {
            this(completionTokens, promptTokens, totalTokens, null);
        }

        /**
         * Breakdown of tokens used in the prompt
         *
         * @param cachedTokens Cached tokens present in the prompt.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record PromptTokensDetails(// @formatter:off
                                          @JsonProperty("cached_tokens") Integer cachedTokens) { // @formatter:on
        }
    }


    public record ChatCompletionMessage(// @formatter:off
                                        @JsonProperty("content") Object rawContent,
                                        @JsonProperty("role") Role role) { // @formatter:on


    }

    public enum Role {

        /**
         * System message.
         */
        @JsonProperty("system") SYSTEM,
        /**
         * User message.
         */
        @JsonProperty("user") USER,
        /**
         * Assistant message.
         */
        @JsonProperty("assistant") ASSISTANT,
        /**
         * Tool message.
         */
        @JsonProperty("tool") TOOL

    }
}
