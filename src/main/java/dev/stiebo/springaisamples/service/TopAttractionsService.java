package dev.stiebo.springaisamples.service;

import dev.stiebo.springaisamples.dto.TopAttractionsOutDto;

public interface TopAttractionsService {
    TopAttractionsOutDto getTopAttractions(String location);
}
