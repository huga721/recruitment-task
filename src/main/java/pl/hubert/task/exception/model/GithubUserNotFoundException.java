package pl.hubert.task.exception.model;

public class GithubUserNotFoundException extends RuntimeException {
    public GithubUserNotFoundException(String message) {
        super(message);
    }

    public GithubUserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public GithubUserNotFoundException(Throwable cause) {
        super(cause);
    }
}
