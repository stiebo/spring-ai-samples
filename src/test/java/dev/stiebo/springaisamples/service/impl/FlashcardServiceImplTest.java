package dev.stiebo.springaisamples.service.impl;

import dev.stiebo.springaisamples.dto.Flashcard;
import dev.stiebo.springaisamples.dto.Flashcards;
import dev.stiebo.springaisamples.exception.FileErrorException;
import dev.stiebo.springaisamples.model.FileResource;
import dev.stiebo.springaisamples.model.Mapper;
import dev.stiebo.springaisamples.service.ChatClientService;
import dev.stiebo.springaisamples.service.UtilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.model.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlashcardServiceImplTest {

    @Mock
    private ChatClientService chatClientService;

    @Mock
    private UtilityService utilityService;

    private Resource mockFlashcardsCsvPrompt;

    private Mapper mapper;

    @InjectMocks
    private FlashcardServiceImpl flashcardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // flashcardsCsvPrompt is injected via @Value, for testing inject the mock resource directly into the
        // private field using ReflectionTestUtils
        mockFlashcardsCsvPrompt = new ByteArrayResource("mockPrompt".getBytes());
        ReflectionTestUtils.setField(flashcardService, "flashcardsPrompt", mockFlashcardsCsvPrompt);
        mapper = new Mapper();
    }

    private void testCreateFlashcardsFromFileWithAllowedType(String fileExtension, String contentType) {
        MultipartFile file = new MockMultipartFile("file", "test" + fileExtension,
                contentType, new byte[0]);
        Flashcards flashcards = new Flashcards(List.of(new Flashcard("Question1", "Answer1")));
        Resource mockImageFileResource = new ByteArrayResource("mockResource".getBytes());
        String mockPdfText = "PdfText";
        FileResource fileResource = mapper.multipartFileToFileResource(file);
        List<Media> media = List.of(new Media(MimeTypeUtils.parseMimeType(contentType), fileResource.resource()));

        when(utilityService.convertPdfToText(fileResource)).thenReturn(mockPdfText);
        when(chatClientService.getResponse(eq(Flashcards.class), eq(mockFlashcardsCsvPrompt), anyList()))
                .thenReturn(flashcards);
        when(chatClientService.getResponse(eq(Flashcards.class), eq(mockFlashcardsCsvPrompt), eq(mockPdfText)))
                .thenReturn(flashcards);

        List<Flashcard> result = flashcardService.createFlashcardsFromFile(fileResource,3);

        assertEquals(1, result.size());
        assertEquals("Question1", result.getFirst().question());
        assertEquals("Answer1", result.getFirst().answer());
    }

    @Test
    void testCreateFlashcardsFromFileWithAllowedFileTypes() {
        testCreateFlashcardsFromFileWithAllowedType(".jpg", "image/jpeg");
        testCreateFlashcardsFromFileWithAllowedType(".jpeg", "image/jpeg");
        testCreateFlashcardsFromFileWithAllowedType(".jpg", "something/else");
        testCreateFlashcardsFromFileWithAllowedType(".png", "image/png");
        testCreateFlashcardsFromFileWithAllowedType(".png", "something/else");
        testCreateFlashcardsFromFileWithAllowedType(".gif", "image/gif");
        testCreateFlashcardsFromFileWithAllowedType(".gif", "something/else");
        testCreateFlashcardsFromFileWithAllowedType(".pdf", "application/pdf");
        testCreateFlashcardsFromFileWithAllowedType(".pdf", "something/else");
        testCreateFlashcardsFromFileWithAllowedType(".somethingElse", "image/jpeg");
        testCreateFlashcardsFromFileWithAllowedType(".somethingElse", "image/png");
        testCreateFlashcardsFromFileWithAllowedType(".somethingElse", "image/gif");
        testCreateFlashcardsFromFileWithAllowedType(".somethingElse", "application/pdf");
    }

    @Test
    void testCreateFlashcardsFromFileWithWrongFileTypesThrowsFileErrors() {
        MultipartFile file = new MockMultipartFile("file", "test.abc",
                "invalid", new byte[0]);
        FileResource fileResource = mapper.multipartFileToFileResource(file);
        assertThrows(FileErrorException.class, () ->
                flashcardService.createFlashcardsFromFile(fileResource,1));
    }

    @Test
    void testCreateFlashcardsFromFileWithWrongGetContentTypeResponseThrowsIllegalStateException() {
        MultipartFile file = new MockMultipartFile("file", "test.jpg",
                "image/jpeg", new byte[0]);
        FileResource fileResource = mapper.multipartFileToFileResource(file);
        FlashcardServiceImpl spyFlashcardService = Mockito.spy(flashcardService);

        doReturn("invalid").when(spyFlashcardService).getContentType(fileResource);

        assertThrows(IllegalStateException.class, () ->
                spyFlashcardService.createFlashcardsFromFile(fileResource,1));
    }

    @Test
    void testConvertToCsv() {
        List<Flashcard> flashcards = List.of(
                new Flashcard("Question1", "Answer1"),
                new Flashcard("Question2", "Answer2"));
        byte[] expected = ("Question1\tAnswer1" + System.lineSeparator() +
                "Question2\tAnswer2" + System.lineSeparator()).getBytes();

        byte[] result = flashcardService.convertToCsv(flashcards);

        assertArrayEquals(expected, result);
    }

    @Test
    void testGetContentTypeOrThrowException_validTypes() throws FileErrorException {
        MultipartFile jpegFile = new MockMultipartFile("file", "test.jpg",
                "image/jpeg", new byte[0]);
        MultipartFile pdfFile = new MockMultipartFile("file", "test.pdf",
                "application/pdf", new byte[0]);

        assertEquals("image/jpeg", flashcardService.getContentType(
                mapper.multipartFileToFileResource(jpegFile)));
        assertEquals("application/pdf", flashcardService.getContentType(
                mapper.multipartFileToFileResource(pdfFile)));
    }

    @Test
    void testGetContentTypeOrThrowException_invalidType() {
        MultipartFile invalidFile = new MockMultipartFile("file", "test.txt", "text/plain", new byte[0]);

        assertThrows(FileErrorException.class, () -> flashcardService.getContentType(
                mapper.multipartFileToFileResource(invalidFile)));
    }
}