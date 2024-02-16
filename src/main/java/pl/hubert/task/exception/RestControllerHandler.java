package pl.hubert.task.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.hubert.task.exception.model.GithubUserNotFoundException;

@Slf4j
@RestControllerAdvice
public class RestControllerHandler {

    @ExceptionHandler(GithubUserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionMessage handleGithubUserNotFoundException(GithubUserNotFoundException ex){
        ExceptionMessage exceptionMessage = ExceptionMessage
                .builder()
                .message(ex.getMessage())
                .status(404)
                .build();
        log.error("Exception has been occurred", ex);
        return exceptionMessage;
    }
}