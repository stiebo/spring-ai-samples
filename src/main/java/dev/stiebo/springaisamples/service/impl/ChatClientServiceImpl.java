package dev.stiebo.springaisamples.service.impl;

import dev.stiebo.springaisamples.service.ChatClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.model.Media;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
public class ChatClientServiceImpl implements ChatClientService {
    private final ChatClient chatClient;

    public ChatClientServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    private <T> T generateResponse(Class<T> responseType, Resource userPromptResource,
                                   List<Media> mediaList, String document) {
        String userPrompt;
        try {
            userPrompt = userPromptResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error reading userPromptResource");
        }
        BeanOutputConverter<T> beanOutputConverter = new BeanOutputConverter<>(responseType);
        String format = beanOutputConverter.getFormat();
        String promptString = "%s\n%s\n%s".formatted(userPrompt, format,
                document != null ? "CONTENT\n%s".formatted(document) : "");
        UserMessage userMessage = (mediaList == null)
                ? new UserMessage(promptString)
                : new UserMessage(promptString, mediaList);
        Prompt prompt = new Prompt(userMessage);
        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
        log.info("Chat client consumed {} tokens, including {} input and {} output tokens.",
                response.getMetadata().getUsage().getTotalTokens(),
                response.getMetadata().getUsage().getPromptTokens(),
                response.getMetadata().getUsage().getGenerationTokens());
        return beanOutputConverter.convert(response.getResult().getOutput().getContent());
    }

    @Override
    public <T> T getResponse(Class<T> responseType, Resource userPromptResource, List<Media> mediaList) {
        return generateResponse(responseType, userPromptResource, mediaList, null);
    }

    @Override
    public <T> T getResponse(Class<T> responseType, Resource userPromptResource, String document) {
        return generateResponse(responseType, userPromptResource, null, document);
    }
}
