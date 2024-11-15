package dev.stiebo.aiutilities.controller;

import dev.stiebo.aiutilities.dto.Flashcard;
import dev.stiebo.aiutilities.exception.ErrorResponse;
import dev.stiebo.aiutilities.exception.ValidationErrorResponse;
import dev.stiebo.aiutilities.model.Mapper;
import dev.stiebo.aiutilities.service.FlashcardService;
import dev.stiebo.aiutilities.validation.NotEmptyFile;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class FlashcardController {

    private final FlashcardService flashcardService;
    private final Mapper mapper;

    @Autowired
    public FlashcardController(FlashcardService flashcardService, Mapper mapper) {
        this.flashcardService = flashcardService;
        this.mapper = mapper;
    }

    @Hidden
    @PostMapping(value = "/createCsvFlashcards", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public byte[] createCSvFlashcards(@RequestParam("file") @NotEmptyFile MultipartFile file) {
        return flashcardService.createCsvFlashcardsFromFile(mapper.multipartFileToFileResource(file));
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
        return flashcardService.createFlashcardsFromFile(mapper.multipartFileToFileResource(file));
    }

}
