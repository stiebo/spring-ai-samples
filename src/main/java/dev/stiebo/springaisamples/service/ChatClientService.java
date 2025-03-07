package dev.stiebo.springaisamples.service;

import org.springframework.ai.model.Media;
import org.springframework.core.io.Resource;

import java.util.List;

public interface ChatClientService {
    <T> T getResponse(Class<T> responseType, Resource userPromptResource, List<Media> mediaList);
    <T> T getResponse(Class<T> responseType, Resource userPromptResource, String document);
}
