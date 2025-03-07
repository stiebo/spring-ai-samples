package dev.stiebo.springaisamples.controller;

import dev.stiebo.springaisamples.dto.Flashcard;
import dev.stiebo.springaisamples.exception.ErrorResponse;
import dev.stiebo.springaisamples.exception.ValidationErrorResponse;
import dev.stiebo.springaisamples.model.Mapper;
import dev.stiebo.springaisamples.service.FlashcardService;
import dev.stiebo.springaisamples.validation.NotEmptyFile;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @Operation(summary = "Create Flashcards as CSV",
            description = """
                    Generates study flashcards from text (pdf) or image files with AI and returns them as a CSV file.
                    For PDF files, an option parameter (1, 2, or 3) can be provided to specify the processing method.
                    
                    Option 1: convert pdf to list of images and call AI once for entire pdf using multimodal
                    -> creates much shorter Q&A but is faster
                    
                    Option 2: convert pdf to images and call AI separately for each image (1 image per page)
                    -> creates longer Q&A but is way slower and more expensive
                    
                    Option 3: convert pdf to text-only
                    -> creates longer Q&A but only works if pdf is text-based and ignores embedded images
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flashcards generated",
                    content = @Content(mediaType = "text/csv",
                            array = @ArraySchema(schema = @Schema(type = "string", format = "binary")))),
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
    @PostMapping(value = "/createCsvFlashcards", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> createCSvFlashcards(@RequestParam("file") @NotEmptyFile MultipartFile file,
                                                      @RequestParam(value = "option", required = false, defaultValue = "1") int option) {
        byte[] csvData = flashcardService.createCsvFlashcardsFromFile(mapper.multipartFileToFileResource(file), option);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=flashcards.csv")
                .body(csvData);
    }

    @Operation(summary = "Create Flashcards",
            description = """
                    Generates study flashcards from text (pdf) or image files with AI.
                    Image files will be analyzed using multimodal GPT.
                    For PDF files, an option parameter (1, 2, or 3) can be provided to specify the processing method.
                    
                    Option 1: convert pdf to list of images and call AI once for entire pdf using multimodal.
                    -> creates much shorter Q&A but is faster
                    
                    Option 2: convert pdf to images and call AI separately for each image (1 image per page)
                    -> creates longer Q&A but is way slower and more expensive
                    
                    Option 3: convert pdf to text-only
                    -> creates longer Q&A but only works if pdf is text-based and ignores embedded images
                    """)
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
                                            @NotEmptyFile MultipartFile file,
                                            @RequestParam(value = "option", required = false, defaultValue = "1")
                                            int option) {
        return flashcardService.createFlashcardsFromFile(mapper.multipartFileToFileResource(file), option);
    }

}
