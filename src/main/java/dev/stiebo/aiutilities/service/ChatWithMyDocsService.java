package dev.stiebo.aiutilities.service;

import dev.stiebo.aiutilities.dto.DocsOutDto;
import dev.stiebo.aiutilities.model.FileResource;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatWithMyDocsService {
    void addDocument (FileResource fileResource);
    List<DocsOutDto> listDocuments ();
    void deleteDocument (String documentName);
    Flux<String> chat (String question);
}
