package pl.hubert.task.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExceptionMessage {
    private int status;
    private String message;
}
