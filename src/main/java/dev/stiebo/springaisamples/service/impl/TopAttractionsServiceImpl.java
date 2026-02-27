package dev.stiebo.springaisamples.service.impl;

import dev.stiebo.springaisamples.dto.TopAttractionsOutDto;
import dev.stiebo.springaisamples.service.ChatClientService;
import dev.stiebo.springaisamples.service.TopAttractionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class TopAttractionsServiceImpl implements TopAttractionsService {

    private final ChatClientService chatClientService;

    @Value("classpath:/prompts/topattractionsprompt.st")
    private Resource topAttractionsPrompt;

    @Autowired
    public TopAttractionsServiceImpl(ChatClientService chatClientService) {
        this.chatClientService = chatClientService;
    }

    @Override
    public TopAttractionsOutDto getTopAttractions(String location) {
        return chatClientService.getResponse(TopAttractionsOutDto.class, topAttractionsPrompt, location);
    }
}
