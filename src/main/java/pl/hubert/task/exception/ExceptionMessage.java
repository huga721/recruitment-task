package pl.hubert.task.exception;

import lombok.Data;

@Data
public class ExceptionMessage {
    private int status;
    private String message;
}
