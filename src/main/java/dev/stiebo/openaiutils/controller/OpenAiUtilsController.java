package dev.stiebo.openaiutils.controller;

import dev.stiebo.openaiutils.dto.CVDataOutDto;
import dev.stiebo.openaiutils.dto.Flashcard;
import dev.stiebo.openaiutils.service.CVService;
import dev.stiebo.openaiutils.service.FlashcardService;
import dev.stiebo.openaiutils.validation.NotEmptyFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/openaiutils")
@Validated
public class OpenAiUtilsController {
    private final CVService cvService;
    private final FlashcardService flashcardService;
    private final ChatWithMyDocsService chatWithMyDocsService;

    @Autowired
    public OpenAiUtilsController(CVService cvService, FlashcardService flashcardService) {
        this.cvService = cvService;
        this.flashcardService = flashcardService;
    }

    private void confirmFileExistsOrThrowException (MultipartFile file) {

    }

    @PostMapping("/analyzeCV")
    public CVDataOutDto analyzeCv(@RequestParam(value = "file", required = false)
                                      @NotEmptyFile MultipartFile file) {
        return cvService.getCVData(file);
    }

    @PostMapping("/createCsvFlashcards")
    public byte[] createCSvFlashcards (@RequestParam("file") @NotEmptyFile MultipartFile file) {
        return flashcardService.createCsvFlashcardsFromFile(file);
    }

    @PostMapping("/createFlashcards")
    public List<Flashcard> createFlashcards (@RequestParam(value = "file", required = false)
                                                 @NotEmptyFile MultipartFile file) {
        return flashcardService.createFlashcardsFromFile(file);
    }

    @PostMapping("/chatwithmydocs/adddoc")
    public Map<String,String> addDocToRepository (@RequestParam(value = "file", required = false)
                                                      @NotEmptyFile MultipartFile file) {
        return Map.of("response", "File has been added to your document store.");
    }
}
