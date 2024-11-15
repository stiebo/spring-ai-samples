package dev.stiebo.aiutilities.service.impl;

import dev.stiebo.aiutilities.dto.DocsOutDto;
import dev.stiebo.aiutilities.exception.FileErrorException;
import dev.stiebo.aiutilities.model.FileResource;
import dev.stiebo.aiutilities.service.ChatWithMyDocsService;
import dev.stiebo.aiutilities.service.UtilityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
public class ChatWithMyDocsServiceImpl implements ChatWithMyDocsService {
    private final VectorStore vectorStore;
    private final JdbcClient jdbcClient;
    private final ChatClient chatClient;
    private final UtilityService utilityService;

    /**
     * Constructs an instance of the ChatWithMyDocsServiceImpl class and injects ChatMemory and
     * QuestionAnswerAdvisor into the chatClient. QuestionAnswerAdvisor enables the chatClient to use
     * a vector store to do SimilaritySearch later on using RAG (Retrieval-Augmented Generation).
     * * For further details, please refer to the documentation:
     *      * <a href="https://docs.spring.io/spring-ai/reference/api/chatclient.html">
     *      * https://docs.spring.io/spring-ai/reference/api/chatclient.html</a>
     *
     * @param vectorStore      the vector store used for managing vectors
     * @param jdbcClient       the JDBC client for database operations
     * @param builder          the ChatClient.Builder for constructing the chat client
     * @param utilityService   the utility service for miscellaneous supportive operations
     * @param resourceLoader   the resource loader for accessing system resources
     */
    @Autowired
    public ChatWithMyDocsServiceImpl(VectorStore vectorStore, JdbcClient jdbcClient, ChatClient.Builder builder,
                                     UtilityService utilityService, ResourceLoader resourceLoader) {
        this.vectorStore = vectorStore;
        this.jdbcClient = jdbcClient;
        this.utilityService = utilityService;
        Resource resourceSystemPrompt =
                resourceLoader.getResource("classpath:/prompts/chatclientsystemprompt.st");
        this.chatClient = builder
                .defaultSystem(resourceSystemPrompt)
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
    }

    private boolean existsByDocumentName(String documentName) {
        return jdbcClient.sql(
                        "SELECT EXISTS (SELECT 1 FROM vector_store WHERE metadata->>'file_name' = :file_name)")
                .param("file_name", documentName)
                .query(Boolean.class)
                .single();
    }

    /**
     * Adds a PDF document to the system, validates its type, and checks for duplicate names.
     * The document is subsequently split into multiple parts and added to a Vectorstore.
     * For further details, please refer to the documentation:
     * <a href="https://docs.spring.io/spring-ai/reference/api/vectordbs.html">
     * https://docs.spring.io/spring-ai/reference/api/vectordbs.html</a>
     *
     * @param fileResource the PDF file to be added.
     * @throws FileErrorException if the file is not a PDF or if a document with the same name already exists.
     */
    @Override
    public void addDocument(FileResource fileResource) throws FileErrorException {
        utilityService.confirmPdfDocumentType(fileResource);
        if (existsByDocumentName(fileResource.fileName())) {
            throw new FileErrorException("Document with that name already exists in database");
        }
        log.info("Loading doc into Vectorstore");
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder()
                        .withNumberOfBottomTextLinesToDelete(0)
                        .build())
                .withPagesPerDocument(1)
                .build();

        PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(fileResource.resource(), config);
        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
        List<Document> docs = tokenTextSplitter.apply(pagePdfDocumentReader.get());
        vectorStore.accept(docs);
        log.info("Vectorstore is ready");
    }

    @Override
    public List<DocsOutDto> listDocuments() {
        return jdbcClient.sql(
                        "SELECT DISTINCT metadata->>'file_name' AS document_name " +
                                "FROM vector_store " +
                                "WHERE metadata->>'file_name' IS NOT NULL")
                .query(DocsOutDto.class).list();
    }

    @Override
    public void deleteDocument(String documentName) throws FileErrorException {
        if (!existsByDocumentName(documentName)) {
            throw new FileErrorException("Document not found in database");
        }
        jdbcClient.sql("DELETE FROM vector_store WHERE metadata->>'file_name' = :file_name")
                .param("file_name", documentName)
                .update();
    }

    @Override
    public Flux<String> chat(String question) {
        return chatClient.prompt()
                .user(question)
                .stream()
                .content();
    }

}
