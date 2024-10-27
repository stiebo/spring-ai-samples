package dev.stiebo.openaiutils.controller;

import dev.stiebo.openaiutils.dto.CVDataOutDto;
import dev.stiebo.openaiutils.exception.FileErrorException;
import dev.stiebo.openaiutils.service.CVService;
import dev.stiebo.openaiutils.service.FlashcardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/openaiutils")
@Validated
public class OpenAiUtilsController {
    CVService cvService;
    FlashcardService flashcardService;

    @Autowired
    public OpenAiUtilsController(CVService cvService, FlashcardService flashcardService) {
        this.cvService = cvService;
        this.flashcardService = flashcardService;
    }

    @PostMapping("/analyzeCV")
    public CVDataOutDto analyzeCv(@RequestParam(value = "file", required = false) MultipartFile file) {
        // TODO could move this to custom @NotEmptyFile annotation
        if (file == null || file.isEmpty()) {
            throw new FileErrorException("Uploaded file is empty. Please upload a valid file.");
        }
        return cvService.getCVData(file);
    }

    @PostMapping("/createFlashcards")
    public byte[] createFlashcards (@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileErrorException("Uploaded file is empty. Please upload a valid file.");
        }
        return flashcardService.createFlashcardsFromFile(file);
    }


}
