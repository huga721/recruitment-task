package pl.hubert.task.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@RestControllerAdvice
public class RestControllerHandler {

    @ExceptionHandler(WebClientResponseException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionMessage handleWebClientException(WebClientResponseException ex){
        ExceptionMessage exceptionMessage = ExceptionMessage
                .builder()
                .message(ex.getMessage())
                .status(ex.getStatusCode().value())
                .build();
        log.error("Exception has been occurred", ex);
        return exceptionMessage;
    }
}