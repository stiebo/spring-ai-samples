package dev.stiebo.openaiutils.service.impl;

import dev.stiebo.openaiutils.dto.CVDataOutDto;
import dev.stiebo.openaiutils.exception.FileErrorException;
import dev.stiebo.openaiutils.service.CVService;
import dev.stiebo.openaiutils.service.ChatClientService;
import dev.stiebo.openaiutils.service.UtilityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Slf4j
@Service
public class CVServiceImpl implements CVService {

    ChatClientService chatClientService;
    UtilityService utilityService;

    @Value("classpath:/prompts/cvprompttemplate.st")
    Resource cvPromptTemplate;

    @Autowired
    public CVServiceImpl(ChatClientService chatClientService, UtilityService utilityService) {
        this.chatClientService = chatClientService;
        this.utilityService = utilityService;
    }

    @Override
    public CVDataOutDto getCVData(MultipartFile file) {
        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            throw new FileErrorException("Invalid File type, only pdf accepted here.");
        }
        String document = utilityService.convertPdfToText(file);
        return chatClientService.getResponse(CVDataOutDto.class, cvPromptTemplate, document);

    }
}
