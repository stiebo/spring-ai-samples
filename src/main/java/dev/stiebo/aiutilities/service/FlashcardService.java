package dev.stiebo.aiutilities.service;

import dev.stiebo.aiutilities.dto.Flashcard;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FlashcardService {
    byte[] createCsvFlashcardsFromFile(MultipartFile file);
    List<Flashcard> createFlashcardsFromFile(MultipartFile file);
}
