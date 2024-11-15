package dev.stiebo.aiutilities.service;

import dev.stiebo.aiutilities.dto.CVDataOutDto;
import dev.stiebo.aiutilities.model.FileResource;

public interface CVService {
    CVDataOutDto getCVData (FileResource fileResource);
}
