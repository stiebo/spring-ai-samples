package dev.stiebo.aiutilities.service.impl;

import dev.stiebo.aiutilities.exception.FileErrorException;
import dev.stiebo.aiutilities.service.UtilityService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Service
public class UtilityServiceImpl implements UtilityService {
    @Override
    public String convertPdfToText(MultipartFile file) {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            return pdfTextStripper.getText(document);
        } catch (IOException e) {
            throw new FileErrorException("Error loading PDF.");
        }
    }

    @Override
    public Resource convertImageFileToResource(MultipartFile file) {
        try {
            return new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
        } catch (IOException e) {
            throw new RuntimeException("Error processing image file");
        }
    }

    @Override
    public void confirmPdfDocumentTypeOrThrow(MultipartFile file) throws FileErrorException {
        if (!Objects.equals(file.getContentType(), "application/pdf") &&
                // Fallback: determine the content type based on file extension
                !file.getOriginalFilename().endsWith(".pdf")) {
            throw new FileErrorException("Invalid File type, only pdf accepted here.");
        }
    }
}
