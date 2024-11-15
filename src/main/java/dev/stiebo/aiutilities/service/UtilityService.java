package dev.stiebo.aiutilities.service;

import dev.stiebo.aiutilities.model.FileResource;

public interface UtilityService {
    String convertPdfToText(FileResource fileResource);
    void confirmPdfDocumentType(FileResource fileResource);
}
