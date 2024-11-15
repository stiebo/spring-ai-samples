package dev.stiebo.springaisamples.service;

import dev.stiebo.springaisamples.dto.CVDataOutDto;
import dev.stiebo.springaisamples.model.FileResource;

public interface CVService {
    CVDataOutDto getCVData (FileResource fileResource);
}
