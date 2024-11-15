package dev.stiebo.springaisamples.service;

import dev.stiebo.springaisamples.dto.DocsOutDto;
import dev.stiebo.springaisamples.model.FileResource;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatWithMyDocsService {
    void addDocument (FileResource fileResource);
    List<DocsOutDto> listDocuments ();
    void deleteDocument (String documentName);
    Flux<String> chat (String question);
}
