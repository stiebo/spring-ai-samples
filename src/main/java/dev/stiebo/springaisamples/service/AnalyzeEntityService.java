package dev.stiebo.springaisamples.service;

import dev.stiebo.springaisamples.dto.EntityDetails;
import dev.stiebo.springaisamples.model.FileResource;
import org.springframework.http.ResponseEntity;

public interface AnalyzeEntityService {
    EntityDetails analyzeEntity(String entityName);
    ResponseEntity<byte[]> analyzeEntitiesXls(FileResource fileResource);
}
