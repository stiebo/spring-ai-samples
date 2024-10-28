package dev.stiebo.openaiutils.controller;

import dev.stiebo.openaiutils.dto.CVDataOutDto;
import dev.stiebo.openaiutils.dto.ChatInDto;
import dev.stiebo.openaiutils.dto.Flashcard;
import dev.stiebo.openaiutils.dto.DocsOutDto;
import dev.stiebo.openaiutils.service.CVService;
import dev.stiebo.openaiutils.service.ChatWithMyDocsService;
import dev.stiebo.openaiutils.service.FlashcardService;
import dev.stiebo.openaiutils.validation.NotEmptyFile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

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
    public OpenAiUtilsController(CVService cvService, FlashcardService flashcardService,
                                 ChatWithMyDocsService chatWithMyDocsService) {
        this.cvService = cvService;
        this.flashcardService = flashcardService;
        this.chatWithMyDocsService = chatWithMyDocsService;
    }

    @PostMapping(value = "/analyzeCV", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CVDataOutDto analyzeCv(@RequestParam(value = "file", required = false)
                                  @NotEmptyFile MultipartFile file) {
        return cvService.getCVData(file);
    }

    @PostMapping(value = "/createCsvFlashcards", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public byte[] createCSvFlashcards(@RequestParam("file") @NotEmptyFile MultipartFile file) {
        return flashcardService.createCsvFlashcardsFromFile(file);
    }

    @PostMapping(value = "/createFlashcards", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<Flashcard> createFlashcards(@RequestParam(value = "file", required = false)
                                            @NotEmptyFile MultipartFile file,
                                            HttpServletRequest request) {
        return flashcardService.createFlashcardsFromFile(file);
    }

    @PostMapping(value = "/chatwithmydocs/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> addDocToRepository(@RequestParam(value = "file", required = false)
                                                  @NotEmptyFile MultipartFile file) {
        chatWithMyDocsService.addDocument(file);
        return Map.of("response", "Document has been added");
    }

    @GetMapping("/chatwithmydocs/documents")
    public List<DocsOutDto> listDocuments() {
        return chatWithMyDocsService.listDocuments();
    }

    @DeleteMapping("/chatwithmydocs/documents/{document_name}")
    public Map<String, String> deleteDocument(@PathVariable("document_name") @NotBlank String documentName) {
        chatWithMyDocsService.deleteDocument(documentName);
        return Map.of("response", "Document has been removed");
    }

    @PostMapping("/chatwithmydocs/chat")
    Flux<String> chat(@Valid @RequestBody ChatInDto chatInDto) {
        return chatWithMyDocsService.chat(chatInDto.question());
    }
}
