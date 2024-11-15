package dev.stiebo.springaisamples.exception;

public class FileErrorException extends RuntimeException {
    public FileErrorException(String message) {
        super(message);
    }
}
