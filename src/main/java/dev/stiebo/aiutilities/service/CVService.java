package dev.stiebo.aiutilities.service;

import dev.stiebo.aiutilities.dto.CVDataOutDto;
import org.springframework.web.multipart.MultipartFile;

public interface CVService {
    CVDataOutDto getCVData (MultipartFile file);
}
