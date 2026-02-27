package dev.stiebo.springaisamples.controller;

import dev.stiebo.springaisamples.dto.LocationInDto;
import dev.stiebo.springaisamples.dto.TopAttractionsOutDto;
import dev.stiebo.springaisamples.exception.ValidationErrorResponse;
import dev.stiebo.springaisamples.service.TopAttractionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Validated
public class TopAttractionsController {

    private final TopAttractionsService topAttractionsService;

    @Autowired
    public TopAttractionsController(TopAttractionsService topAttractionsService) {
        this.topAttractionsService = topAttractionsService;
    }

    @Operation(summary = "Get Top Attractions",
            description = "Accepts a city, country, or region and returns the top 10 attractions to visit. " +
                    "Requires TOURGUIDE or ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attractions generated",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TopAttractionsOutDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for request body",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "TOURGUIDE or ADMIN role required")
    })
    @PostMapping("/top-attractions")
    public TopAttractionsOutDto getTopAttractions(@RequestBody @Valid LocationInDto request) {
        return topAttractionsService.getTopAttractions(request.location());
    }
}
