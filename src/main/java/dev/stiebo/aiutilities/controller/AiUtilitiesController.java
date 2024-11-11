package dev.stiebo.aiutilities.controller;

import dev.stiebo.aiutilities.dto.CVDataOutDto;
import dev.stiebo.aiutilities.dto.ChatInDto;
import dev.stiebo.aiutilities.dto.Flashcard;
import dev.stiebo.aiutilities.dto.DocsOutDto;
import dev.stiebo.aiutilities.exception.ErrorResponse;
import dev.stiebo.aiutilities.exception.ValidationErrorResponse;
import dev.stiebo.aiutilities.service.CVService;
import dev.stiebo.aiutilities.service.ChatWithMyDocsService;
import dev.stiebo.aiutilities.service.FlashcardService;
import dev.stiebo.aiutilities.validation.NotEmptyFile;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api")
@Validated
public class AiUtilitiesController {
    private final CVService cvService;
    private final FlashcardService flashcardService;
    private final ChatWithMyDocsService chatWithMyDocsService;

    @Autowired
    public AiUtilitiesController(CVService cvService, FlashcardService flashcardService,
                                 ChatWithMyDocsService chatWithMyDocsService) {
        this.cvService = cvService;
        this.flashcardService = flashcardService;
        this.chatWithMyDocsService = chatWithMyDocsService;
    }

    @Operation(summary = "Analyze CV",
            description = "Analyze any CV and get a breakdown of its content in json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CV analyzed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CVDataOutDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for request parameter",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class)
                    )),
            @ApiResponse(responseCode = "422", description = "File Error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    @PostMapping(value = "/analyzeCV", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CVDataOutDto analyzeCv(@RequestParam(value = "file", required = false)
                                  @NotEmptyFile MultipartFile file) {
        return cvService.getCVData(file);
    }

    @Hidden
    @PostMapping(value = "/createCsvFlashcards", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public byte[] createCSvFlashcards(@RequestParam("file") @NotEmptyFile MultipartFile file) {
        return flashcardService.createCsvFlashcardsFromFile(file);
    }

    @Operation(summary = "Create Flashcards",
            description = "Generates study flashcards from text (pdf) or image files with AI")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flashcards generated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = Flashcard.class)))),
            @ApiResponse(responseCode = "400", description = "Validation failed for request parameter",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class)
                    )),
            @ApiResponse(responseCode = "422", description = "File Error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    @PostMapping(value = "/createFlashcards", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<Flashcard> createFlashcards(@RequestParam(value = "file", required = false)
                                            @NotEmptyFile MultipartFile file) {
        return flashcardService.createFlashcardsFromFile(file);
    }

    @Operation(summary = "Add Document to Repository",
            description = "Add a PDF File to the \"Chat with my Docs\"-Repository")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"status\":\"Document has been added\"}"))),
            @ApiResponse(responseCode = "400", description = "Validation failed for request parameter",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class)
                    )),
            @ApiResponse(responseCode = "422", description = "File Error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    @PostMapping(value = "/chatwithmydocs/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> addDocToRepository(@RequestParam(value = "file", required = false)
                                                  @NotEmptyFile MultipartFile file) {
        chatWithMyDocsService.addDocument(file);
        return Map.of("status", "Document has been added");
    }

    @Operation(summary = "List Documents",
            description = "List all documents in the \"Chat with my Docs\"-Repository")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = DocsOutDto.class))))
    })
    @GetMapping("/chatwithmydocs/documents")
    public List<DocsOutDto> listDocuments() {
        return chatWithMyDocsService.listDocuments();
    }

    @Operation(summary = "Delete Document from Repository",
            description = "Delete a document from the \"Chat with my Docs\"-Repository")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"status\":\"Document has been removed\"}"))),
            @ApiResponse(responseCode = "400", description = "Validation failed for request parameter",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class)
                    )),
            @ApiResponse(responseCode = "422", description = "File Error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    @DeleteMapping("/chatwithmydocs/documents/{document_name}")
    public Map<String, String> deleteDocument(@PathVariable("document_name") @NotBlank String documentName) {
        chatWithMyDocsService.deleteDocument(documentName);
        return Map.of("status", "Document has been removed");
    }

    @Operation(summary = "Chat Client For My Documents",
            description = "Chat client of the \"Chat with my Docs\"-Repository")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = String.class))))
    })
    @PostMapping("/chatwithmydocs/chat")
    Flux<String> chat(@Valid @RequestBody ChatInDto chatInDto) {
        return chatWithMyDocsService.chat(chatInDto.question());
    }
}
