package dev.stiebo.aiutilities.service.impl;

import dev.stiebo.aiutilities.dto.CVDataOutDto;
import dev.stiebo.aiutilities.service.CVService;
import dev.stiebo.aiutilities.service.ChatClientService;
import dev.stiebo.aiutilities.service.UtilityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    public CVDataOutDto getCVData(MultipartFile file) {
        utilityService.confirmPdfDocumentTypeOrThrow(file);
        String document = utilityService.convertPdfToText(file);
        return chatClientService.getResponse(CVDataOutDto.class, cvPromptTemplate, document);

    }
}
