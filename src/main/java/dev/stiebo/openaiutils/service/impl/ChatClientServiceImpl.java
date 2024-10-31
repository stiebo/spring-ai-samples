package dev.stiebo.openaiutils.service.impl;

import dev.stiebo.openaiutils.service.ChatClientService;
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

@Slf4j
@Service
public class ChatClientServiceImpl implements ChatClientService {
    private final ChatClient chatClient;

    public ChatClientServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    private <T> T generateResponse(Class<T> responseType, Resource userPromptResource, Media media, String document) {
        // extract userPrompt from Resource(-file)
        String userPrompt;
        try {
            userPrompt = userPromptResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error reading userPromptResource");
        }
        // Initialize the BeanOutputConverter with the provided response type
        BeanOutputConverter<T> beanOutputConverter = new BeanOutputConverter<>(responseType);
        // Get the format for the response type
        String format = beanOutputConverter.getFormat();
        // Create the user message and prompt
        String promptString = "%s\n%s\n%s".formatted(userPrompt, format,
                document != null ? "CONTENT\n%s".formatted(document) : "");
        UserMessage userMessage = (media == null)
                ? new UserMessage(promptString)
                : new UserMessage(promptString, media);
        Prompt prompt = new Prompt(userMessage);
        // Call the chat client with the prompt
        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
        // Log the token usage
        log.info("Chat client consumed {} tokens, including {} input and {} output tokens.",
                response.getMetadata().getUsage().getTotalTokens(),
                response.getMetadata().getUsage().getPromptTokens(),
                response.getMetadata().getUsage().getGenerationTokens());
        // Convert the response output to the specified type
        return beanOutputConverter.convert(response.getResult().getOutput().getContent());
    }

    @Override
    public <T> T getResponse(Class<T> responseType, Resource userPromptResource, Resource imageResource) {
        return generateResponse(responseType, userPromptResource,
                new Media(MimeTypeUtils.IMAGE_JPEG, imageResource), null);
    }

    @Override
    public <T> T getResponse(Class<T> responseType, Resource userPromptResource, String document) {
        return generateResponse(responseType, userPromptResource, null, document);
    }
}
