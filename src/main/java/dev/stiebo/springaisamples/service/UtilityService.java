package dev.stiebo.springaisamples.service;

import dev.stiebo.springaisamples.model.FileResource;
import org.springframework.ai.model.Media;
import java.util.List;

public interface UtilityService {
    String convertPdfToText(FileResource fileResource);
    void confirmPdfDocumentType(FileResource fileResource);
    List<Media> convertPdfToImages(FileResource fileResource);
}
