package dev.stiebo.aiutilities.service.impl;

import dev.stiebo.aiutilities.exception.FileErrorException;
import dev.stiebo.aiutilities.model.FileResource;
import dev.stiebo.aiutilities.service.UtilityService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class UtilityServiceImpl implements UtilityService {
    @Override
    public String convertPdfToText(FileResource fileResource) {
        try (PDDocument document = Loader.loadPDF(fileResource.resource().getContentAsByteArray())) {
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            return pdfTextStripper.getText(document);
        } catch (IOException e) {
            throw new FileErrorException("Error loading PDF.");
        }
    }

    /**
     * Confirms if the given file document is a PDF. Throws an exception if the document is not a PDF.
     *
     * @param fileResource The file document to be checked.
     * @throws FileErrorException if the document is not a PDF.
     */
    @Override
    public void confirmPdfDocumentType(FileResource fileResource) throws FileErrorException {
        if (!fileResource.contentType().equals("application/pdf") &&
                // Fallback: determine the content type based on file extension
                !fileResource.fileName().endsWith(".pdf")) {
            throw new FileErrorException("Invalid File type, only pdf accepted here.");
        }
    }
}
