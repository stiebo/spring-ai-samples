package dev.stiebo.openaiutils.service;

import dev.stiebo.openaiutils.dto.CVDataOutDto;
import org.springframework.web.multipart.MultipartFile;

public interface CVService {
    CVDataOutDto getCVData (MultipartFile file);
}
