package dev.stiebo.aiutilities.service;

import dev.stiebo.aiutilities.dto.Flashcard;
import dev.stiebo.aiutilities.model.FileResource;

import java.util.List;

public interface FlashcardService {
    byte[] createCsvFlashcardsFromFile(FileResource fileResource);
    List<Flashcard> createFlashcardsFromFile(FileResource fileResource);
}
