package dev.stiebo.openaiutils.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface UtilityService {
    String convertPdfToText(MultipartFile file);
    Resource convertImageFileToResource(MultipartFile file);

}
