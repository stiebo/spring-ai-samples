package dev.stiebo.openaiutils.service.impl;

import dev.stiebo.openaiutils.dto.CVDataOutDto;
import dev.stiebo.openaiutils.exception.FileErrorException;
import dev.stiebo.openaiutils.service.CVService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class CVServiceImpl implements CVService {

    ChatClient chatClient;

    @Value("classpath:/prompts/cvprompttemplate.st")
    Resource cvPromptTemplate;

    @Autowired
    public CVServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public CVDataOutDto getCVData(MultipartFile file) {
        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            throw new FileErrorException("Invalid File type, only pdf accepted here.");
        }

        String document = convertPdfToText(file);

        BeanOutputConverter<CVDataOutDto> cvDataBeanOutputConverter =
                new BeanOutputConverter<>(CVDataOutDto.class);
        String format = cvDataBeanOutputConverter.getFormat();

        PromptTemplate promptTemplate = new PromptTemplate(cvPromptTemplate);
        Map<String,Object> promptParameters = new HashMap<>();
        promptParameters.put("format", format);
        promptParameters.put("document", document);

        ChatResponse chatResponse = chatClient.prompt(promptTemplate.create(promptParameters)).call().chatResponse();

        log.info("Chat client consumed {} tokens, including {} input and {} output tokens.",
                chatResponse.getMetadata().getUsage().getTotalTokens(),
                chatResponse.getMetadata().getUsage().getPromptTokens(),
                chatResponse.getMetadata().getUsage().getGenerationTokens());

        String response = chatResponse.getResult().getOutput().getContent();
        return cvDataBeanOutputConverter.convert(response);
    }

    private static String convertPdfToText(MultipartFile file) {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            return pdfTextStripper.getText(document);
        }
        catch (IOException e) {
            throw new FileErrorException("Error loading PDF.");
        }
    }
}
