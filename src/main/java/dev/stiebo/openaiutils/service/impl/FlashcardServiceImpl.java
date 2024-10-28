package dev.stiebo.openaiutils.service.impl;

import dev.stiebo.openaiutils.dto.Flashcard;
import dev.stiebo.openaiutils.dto.Flashcards;
import dev.stiebo.openaiutils.exception.FileErrorException;
import dev.stiebo.openaiutils.service.ChatClientService;
import dev.stiebo.openaiutils.service.FlashcardService;
import dev.stiebo.openaiutils.service.UtilityService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

@Service
public class FlashcardServiceImpl implements FlashcardService {
    private final ChatClientService chatClientService;
    private final UtilityService utilityService;

    @Value("classpath:/prompts/flashcardscsvprompt.st")
    private Resource flashcardsCsvPrompt;

    @Autowired
    public FlashcardServiceImpl(ChatClientService chatClientService, UtilityService utilityService) {
        this.chatClientService = chatClientService;
        this.utilityService = utilityService;
    }

    @Override
    public byte[] createCsvFlashcardsFromFile(MultipartFile file) {
        return convertToCsv(createFlashcardsFromFile(file));
    }

    @Override
    public List<Flashcard> createFlashcardsFromFile(MultipartFile file) {
        Flashcards flashcards = switch (file.getContentType()) {
            // call chatClient with (Image-)Resource
            case "image/jpeg", "image/gif", "image/png" -> chatClientService.getResponse(
                    Flashcards.class, flashcardsCsvPrompt, utilityService.convertImageFileToResource(file));
            // call with (String-)document
            case "application/pdf" -> chatClientService.getResponse(
                    Flashcards.class, flashcardsCsvPrompt, utilityService.convertPdfToText(file));
            case null, default -> throw new FileErrorException("Invalid File Type");
        };
        return flashcards.flashcards();
    }

    private byte[] convertToCsv(List<Flashcard> flashcards) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos);
             CSVPrinter csvPrinter = new CSVPrinter(osw,
                     CSVFormat.Builder.create()
                             .setDelimiter('\t')
                             .build())) {
            for (Flashcard flashcard : flashcards) {
                csvPrinter.printRecord(flashcard.question(), flashcard.answer());
            }
            csvPrinter.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error converting to csv");
        }
    }
}
