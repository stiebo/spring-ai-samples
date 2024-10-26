package dev.stiebo.openaiutils.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FileErrorException extends RuntimeException {
    public FileErrorException(String message) {
        super(message);
    }
}
