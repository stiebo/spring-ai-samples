package dev.stiebo.springaisamples.service;

import dev.stiebo.springaisamples.model.FileResource;

public interface UtilityService {
    String convertPdfToText(FileResource fileResource);
    void confirmPdfDocumentType(FileResource fileResource);
}
