package dev.stiebo.springaisamples.service;

import dev.stiebo.springaisamples.dto.CodingAnswerOutDto;

public interface CodingQuestionService {
    CodingAnswerOutDto answerQuestion(String question);
}
