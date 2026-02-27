package dev.stiebo.springaisamples.service.impl;

import dev.stiebo.springaisamples.dto.CodingAnswerOutDto;
import dev.stiebo.springaisamples.service.ChatClientService;
import dev.stiebo.springaisamples.service.CodingQuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CodingQuestionServiceImpl implements CodingQuestionService {

    private final ChatClientService chatClientService;

    @Value("classpath:/prompts/codingquestionprompt.st")
    private Resource codingQuestionPrompt;

    @Autowired
    public CodingQuestionServiceImpl(ChatClientService chatClientService) {
        this.chatClientService = chatClientService;
    }

    @Override
    public CodingAnswerOutDto answerQuestion(String question) {
        return chatClientService.getResponse(CodingAnswerOutDto.class, codingQuestionPrompt, question);
    }
}
