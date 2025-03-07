package dev.stiebo.springaisamples.service.impl;

import dev.stiebo.springaisamples.exception.FileErrorException;
import dev.stiebo.springaisamples.model.FileResource;
import dev.stiebo.springaisamples.service.UtilityService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.model.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<Media> convertPdfToImages(FileResource fileResource) {
        try (PDDocument document = Loader.loadPDF(fileResource.resource().getContentAsByteArray())) {
            List<Media> images = new ArrayList<>();
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 150, ImageType.RGB);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                ByteArrayResource imageResource = new ByteArrayResource(baos.toByteArray());
                images.add(new Media(MimeTypeUtils.IMAGE_PNG, imageResource));
            }
            return images;
        } catch (IOException e) {
            throw new FileErrorException("Error converting PDF to images.");
        }
    }
}
