package dev.stiebo.springaisamples.service.impl;

import dev.stiebo.springaisamples.dto.DocsOutDto;
import dev.stiebo.springaisamples.exception.FileErrorException;
import dev.stiebo.springaisamples.model.FileResource;
import dev.stiebo.springaisamples.model.Mapper;
import dev.stiebo.springaisamples.service.UtilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;


import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatWithMyDocsServiceImplTest {

    @Mock
    private VectorStore vectorStore;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JdbcClient jdbcClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    ChatClient.Builder builder;

    @Mock
    private ChatClient chatClient;

    @Spy
    private UtilityService utilityService = new UtilityServiceImpl();

    @Mock
    private ResourceLoader resourceLoader;

    @Autowired
    private Mapper mapper;

    private ChatWithMyDocsServiceImpl chatWithMyDocsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(builder
                .defaultAdvisors((Advisor) any())
                .defaultAdvisors((Advisor) any())
                .build()).thenReturn(chatClient);

        chatWithMyDocsService = new ChatWithMyDocsServiceImpl(
                vectorStore, jdbcClient, builder, utilityService, resourceLoader);
    }

    private MockMultipartFile getMockMultipartFile() {
        MockMultipartFile file;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test_pdf_file.pdf")) {
            file = new MockMultipartFile(
                    "file",
                    "test_pdf_file.pdf",
                    "application/pdf",
                    inputStream
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    @Test
    void testAddDocumentAddsFileIntoVectorStore() {
        // Convert the file to an InputStream
        MockMultipartFile file = getMockMultipartFile();
        FileResource fileResource = mapper.multipartFileToFileResource(file);
        when(jdbcClient.sql(anyString())
                .param(anyString(), anyString())
                .query(Boolean.class)
                .single())
                .thenReturn(false);
        doNothing().when(vectorStore).accept(anyList());

        chatWithMyDocsService.addDocument(fileResource);

        verify(utilityService, times(1)).confirmPdfDocumentType(fileResource);
        verify(vectorStore, times(1)).accept(anyList());
    }

    @Test
    void testAddDocumentWithWrongTypeThrowsException() {
        MultipartFile file = new MockMultipartFile("test.abc", "test.abc",
                "text/plain", "test data".getBytes());

        assertThrows(FileErrorException.class, () ->
                chatWithMyDocsService.addDocument(mapper.multipartFileToFileResource(file)));
    }

    @Test
    void testAddDocumentThatAlreadyExistsThrowsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.pdf");

        when(jdbcClient.sql(anyString())
                .param(anyString(), anyString())
                .query(Boolean.class)
                .single())
                .thenReturn(true);

        assertThrows(FileErrorException.class, () -> chatWithMyDocsService.addDocument(
                mapper.multipartFileToFileResource(file)
        ));
        verify(vectorStore, never()).accept(anyList());
    }

    @Test
    void testListDocumentsReturnsListDocsOutDto() {
        List<DocsOutDto> mockResult = List.of(
            new DocsOutDto("doc1.pdf"),
                new DocsOutDto("doc2.pdf")
                );
        when(jdbcClient.sql(anyString())
                .query(DocsOutDto.class).list())
                .thenReturn(mockResult);

        List<DocsOutDto> result = chatWithMyDocsService.listDocuments();

        assertEquals(mockResult, result);
    }

    @Test
    void testDeleteDocumentCallsJdbcClientDeleteUpdate() {
        String docName = "file_to_delete.pdf";
        JdbcClient.StatementSpec statementSpecMock = mock(JdbcClient.StatementSpec.class); // Assuming StatementSpec is the correct type returned by jdbcClient.sql(...)
        JdbcClient.MappedQuerySpec<Boolean> mappedQuerySpecMock = mock(JdbcClient.MappedQuerySpec.class); // Specify Boolean as the type parameter

        when(jdbcClient.sql(anyString())
                .param(anyString(), eq(docName))
                .query(Boolean.class)
                .single())
                .thenReturn(true);
        when(jdbcClient.sql(anyString())
                .param(anyString(), eq(docName))
                .update()).thenReturn(1);

        chatWithMyDocsService.deleteDocument(docName);

        verify(jdbcClient, atLeastOnce()).sql(any()).param(anyString(),eq(docName)).update();
        }

//    @Test
//    void deleteDocument_ShouldDeleteDocument_WhenDocumentExists() throws Exception {
//        String documentName = "sample.pdf";
//        when(jdbcClient.sql(anyString())).thenReturn(jdbcClient);
//        when(jdbcClient.param(anyString(), eq(documentName))).thenReturn(jdbcClient);
//        when(jdbcClient.query(Boolean.class)).thenReturn(jdbcClient);
//        when(jdbcClient.single()).thenReturn(true);
//
//        chatWithMyDocsService.deleteDocument(documentName);
//
//        verify(jdbcClient).update();
//    }
//
//    @Test
//    void deleteDocument_ShouldThrowException_WhenDocumentNotFound() {
//        String documentName = "non_existent.pdf";
//        when(jdbcClient.sql(anyString())).thenReturn(jdbcClient);
//        when(jdbcClient.param(anyString(), eq(documentName))).thenReturn(jdbcClient);
//        when(jdbcClient.query(Boolean.class)).thenReturn(jdbcClient);
//        when(jdbcClient.single()).thenReturn(false);
//
//        assertThrows(FileErrorException.class, () -> chatWithMyDocsService.deleteDocument(documentName));
//
//        verify(jdbcClient, never()).update();
//    }
//
//    @Test
//    void chat_ShouldReturnResponse_WhenPromptIsProvided() {
//        String question = "What is AI?";
//        Flux<String> responseFlux = Flux.just("AI is Artificial Intelligence.");
//        when(chatClient.prompt().user(question).stream().content()).thenReturn(responseFlux);
//
//        Flux<String> result = chatWithMyDocsService.chat(question);
//
//        assertEquals(responseFlux, result);
//    }
}