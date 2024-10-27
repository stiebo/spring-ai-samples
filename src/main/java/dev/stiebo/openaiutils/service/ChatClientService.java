package dev.stiebo.openaiutils.service;

import org.springframework.ai.model.Media;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChatClientService {
    <T> T getResponse(Class<T> responseType, Resource userPromptResource);
    <T> T getResponse(Class<T> responseType, Resource userPromptResource, Resource imageResource);
    <T> T getResponse(Class<T> responseType, Resource userPromptResource, String document);
}
