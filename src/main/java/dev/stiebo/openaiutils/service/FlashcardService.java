package dev.stiebo.openaiutils.service;

import org.springframework.web.multipart.MultipartFile;

public interface FlashcardService {
    byte[] createFlashcardsFromFile(MultipartFile file);
}
