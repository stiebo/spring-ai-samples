package dev.stiebo.springaisamples.controller;

import dev.stiebo.springaisamples.dto.EntityDetails;
import dev.stiebo.springaisamples.dto.Flashcard;
import dev.stiebo.springaisamples.exception.ErrorResponse;
import dev.stiebo.springaisamples.exception.ValidationErrorResponse;
import dev.stiebo.springaisamples.model.Mapper;
import dev.stiebo.springaisamples.service.AnalyzeEntityService;
import dev.stiebo.springaisamples.validation.NotEmptyFile;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@Validated
public class AnalyzeEntityController {

    private final AnalyzeEntityService analyzeEntityService;
    private final Mapper mapper;

    @Autowired
    public AnalyzeEntityController(AnalyzeEntityService analyzeEntityService, Mapper mapper) {
        this.analyzeEntityService = analyzeEntityService;
        this.mapper = mapper;
    }

    @Hidden
    @PostMapping("/analyzeentity")
    public EntityDetails submitName(@RequestBody String name) {
        return analyzeEntityService.analyzeEntity(name);
    }

    @Operation(summary = "Analyze Entities",
            description = """
                    Generate information about entities provided in Excel.
                    Expects an Excel spreadsheet in .XLSX with one sheet. The sheet must have only one column
                    with a header called "Name", subsequent rows in this column may contain one or more names of entities.
                    Response will be an Excel file with further columns added to the spreadsheet for download,
                    including type of organisation, short description, country and URL.
                    Information is created via OpenAI API and may not always be correct!!
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Spreadsheet created and filled with information",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = EntityDetails.class)))),
            @ApiResponse(responseCode = "422", description = "File Error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    @PostMapping(value = "/analyze-entities-xls", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> analyzeEntitiesXls(@RequestParam(value = "file", required = false)
                                          @NotEmptyFile MultipartFile file) {
        return analyzeEntityService.analyzeEntitiesXls(mapper.multipartFileToFileResource(file));
    }
}
