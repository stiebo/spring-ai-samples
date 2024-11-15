package dev.stiebo.springaisamples.model;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class Mapper {

    public FileResource multipartFileToFileResource(MultipartFile multipartFile) {
        return new FileResource(
                multipartFile.getOriginalFilename(),
                multipartFile.getResource(),
                multipartFile.getContentType()
        );
    }
}
