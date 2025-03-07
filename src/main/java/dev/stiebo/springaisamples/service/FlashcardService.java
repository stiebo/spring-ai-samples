package dev.stiebo.springaisamples.service;

import dev.stiebo.springaisamples.dto.Flashcard;
import dev.stiebo.springaisamples.model.FileResource;

import java.util.List;

public interface FlashcardService {
    byte[] createCsvFlashcardsFromFile(FileResource fileResource, int option);
    List<Flashcard> createFlashcardsFromFile(FileResource fileResource, int option);
}
