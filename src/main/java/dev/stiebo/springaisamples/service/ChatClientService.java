package dev.stiebo.springaisamples.service;

import org.springframework.core.io.Resource;

public interface ChatClientService {
    <T> T getResponse(Class<T> responseType, Resource userPromptResource, Resource imageResource);
    <T> T getResponse(Class<T> responseType, Resource userPromptResource, String document);
}
