package dev.stiebo.openaiutils.service.impl;

import dev.stiebo.openaiutils.dto.DocsOutDto;
import dev.stiebo.openaiutils.exception.FileErrorException;
import dev.stiebo.openaiutils.service.ChatWithMyDocsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
public class ChatWithMyDocsServiceImpl implements ChatWithMyDocsService {
    private final VectorStore vectorStore;
    private final JdbcClient jdbcClient;
    private final ChatClient chatClient;

    @Autowired
    public ChatWithMyDocsServiceImpl(VectorStore vectorStore,
                                     JdbcClient jdbcClient, ChatClient.Builder builder) {
        this.vectorStore = vectorStore;
        this.jdbcClient = jdbcClient;
        this.chatClient = builder
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

    @Override
    public void addDocument(MultipartFile file) throws FileErrorException {
        if (existsByDocumentName(file.getName())) {
            throw new FileErrorException("Document with that name already exists in database");
        }
        log.info("Loading doc into Vectorstore");
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder()
                        .withNumberOfBottomTextLinesToDelete(0)
                        .build())
                .withPagesPerDocument(1)
                .build();

        PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(file.getResource(), config);
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
