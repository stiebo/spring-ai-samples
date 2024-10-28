package dev.stiebo.openaiutils.service;

import dev.stiebo.openaiutils.dto.DocsOutDto;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatWithMyDocsService {
    void addDocument (MultipartFile file);
    List<DocsOutDto> listDocuments ();
    void deleteDocument (String documentName);
    Flux<String> chat (String question);
}
