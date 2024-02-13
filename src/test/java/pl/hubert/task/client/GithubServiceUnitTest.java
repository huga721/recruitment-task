package pl.hubert.task.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pl.hubert.task.model.Branch;
import pl.hubert.task.model.Repository;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GithubServiceUnitTest {

    @Mock
    private WebClient webClientMock;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock
    private WebClient.ResponseSpec responseMock;
    @InjectMocks
    private GithubClient githubService;

    @Test
    void getUserRepositories_shouldReturnFilteredRepositories() {
        Branch branch = new Branch();
        Repository repository1 = new Repository("user", "repo1", false, List.of(branch));

        when(webClientMock.get())
                .thenReturn(requestHeadersUriMock);

        when(requestHeadersUriMock.uri(eq("/users/{username}/repos"), anyString()))
                .thenReturn(requestHeadersMock);
        when(requestHeadersUriMock.uri(eq("/repos/{ownerLogin}/{repositoryName}/branches"), anyString(), anyString()))
                .thenReturn(requestHeadersMock);

        when(requestHeadersMock.retrieve())
                .thenReturn(responseMock);
        when(requestHeadersMock.retrieve())
                .thenReturn(responseMock);

        when(responseMock.bodyToFlux(Repository.class))
                .thenReturn(Flux.just(repository1));
        when(responseMock.bodyToFlux(Branch.class))
                .thenReturn(Flux.just(branch));

        Flux<Repository> result = githubService.getUserRepositories("user");

        StepVerifier.create(result)
                .expectNextMatches(repo -> repo.getRepositoryName().equals("repo1"))
                .verifyComplete();
    }

    @Test
    void getUserRepositories_shouldNotReturnRepositoryWhenIsFork() {
        Branch branch = new Branch();
        Repository repository1 = new Repository("user", "repo1", true, List.of(branch));

        when(webClientMock.get())
                .thenReturn(requestHeadersUriMock);

        when(requestHeadersUriMock.uri(eq("/users/{username}/repos"), anyString()))
                .thenReturn(requestHeadersMock);
        when(requestHeadersUriMock.uri(eq("/repos/{ownerLogin}/{repositoryName}/branches"), anyString(), anyString()))
                .thenReturn(requestHeadersMock);

        when(requestHeadersMock.retrieve())
                .thenReturn(responseMock);
        when(requestHeadersMock.retrieve())
                .thenReturn(responseMock);

        when(responseMock.bodyToFlux(Repository.class))
                .thenReturn(Flux.just(repository1));
        when(responseMock.bodyToFlux(Branch.class))
                .thenReturn(Flux.just(branch));

        Flux<Repository> result = githubService.getUserRepositories("user");

        StepVerifier.create(result)
                .expectComplete()
                .verify();
    }

    @Test
    void shouldNotGetUserRepositories_WhenUserNotExist() {
        WebClientResponseException notFoundException = WebClientResponseException.create(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                null,
                null,
                null
        );

        when(webClientMock.get())
                .thenReturn(requestHeadersUriMock);

        when(requestHeadersUriMock.uri(eq("/users/{username}/repos"), anyString()))
                .thenThrow(notFoundException);

        assertThrows(WebClientResponseException.class, () -> githubService.getUserRepositories("xd"));
    }
}