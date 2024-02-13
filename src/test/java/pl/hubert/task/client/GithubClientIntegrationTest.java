package pl.hubert.task.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pl.hubert.task.model.Branch;
import pl.hubert.task.model.Repository;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GithubClientIntegrationTest {

    public static MockWebServer mockBackEnd;
    private GithubClient githubClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void initialize() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockBackEnd.url("/").toString())
                .build();
        githubClient = new GithubClient(webClient);
    }

    @Test
    void getUserRepositories_shouldReturnRepositories() throws InterruptedException, JsonProcessingException {
        Branch branch = new Branch("main", "123");
        Repository repository = new Repository("user", "recruitment-task", false, List.of(branch));

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(objectMapper.writeValueAsString(repository))
                .addHeader("Content-Type", "application/json"));

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(objectMapper.writeValueAsString(branch))
                .addHeader("Content-Type", "application/json"));

        Flux<Repository> result = githubClient.getUserRepositories("user");

        StepVerifier.create(result)
                .expectNextMatches(repo -> repo.getRepositoryName().equals("recruitment-task"))
                .verifyComplete();

        assertThat(mockBackEnd.takeRequest().getPath()).isEqualTo("/users/user/repos");
        assertThat(mockBackEnd.takeRequest().getPath()).isEqualTo("/repos/user/recruitment-task/branches");
    }

    @Test
    void shouldNotGetUserRepositories_WhenUserNotExist() {
        mockBackEnd.enqueue(new MockResponse().setResponseCode(404));

        Flux<Repository> result = githubClient.getUserRepositories("nonexistent");

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof WebClientResponseException)
                .verify();
    }
}