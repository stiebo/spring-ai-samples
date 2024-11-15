package dev.stiebo.springaisamples.service.impl;

import dev.stiebo.springaisamples.dto.CVDataOutDto;
import dev.stiebo.springaisamples.model.FileResource;
import dev.stiebo.springaisamples.service.CVService;
import dev.stiebo.springaisamples.service.ChatClientService;
import dev.stiebo.springaisamples.service.UtilityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CVServiceImpl implements CVService {

    private final ChatClientService chatClientService;
    private final UtilityService utilityService;

    @Value("classpath:/prompts/cvprompttemplate.st")
    private Resource cvPromptTemplate;

    @Autowired
    public CVServiceImpl(ChatClientService chatClientService, UtilityService utilityService) {
        this.chatClientService = chatClientService;
        this.utilityService = utilityService;
    }

    @Override
    public CVDataOutDto getCVData(FileResource fileResource) {
        utilityService.confirmPdfDocumentType(fileResource);
        String document = utilityService.convertPdfToText(fileResource);
        return chatClientService.getResponse(CVDataOutDto.class, cvPromptTemplate, document);
    }
}
