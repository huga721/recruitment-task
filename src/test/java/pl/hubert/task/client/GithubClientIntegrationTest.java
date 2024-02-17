package pl.hubert.task.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import pl.hubert.task.exception.model.GithubUserNotFoundException;
import pl.hubert.task.model.Repository;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GithubClientIntegrationTest {

    private WireMockServer mockServer;
    private GithubClient githubClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() throws SecurityException, IllegalArgumentException {
        mockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort().dynamicHttpsPort());
        mockServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:" + mockServer.port())
                .build();
        WireMock.configureFor("localhost", mockServer.port());
        githubClient = new GithubClient(webClient);
    }

    @AfterEach
    public void teardown() {
        mockServer.stop();
    }

    @Test
    public void shouldReturnUserRepositories() throws IOException, URISyntaxException {
        String repos = Files
                .readString(Paths.get(Objects.requireNonNull(getClass().getResource("/repo.json")).toURI()));
        String branch = Files
                .readString(Paths.get(Objects.requireNonNull(getClass().getResource("/branch.json")).toURI()));

        String user = "huga721";
        String repoName = "codewars";

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/users/" + user + "/repos"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(repos)));

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/repos/" + user + "/" + repoName + "/branches"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(branch)));

        Flux<Repository> repositoryFlux = githubClient.getUserRepositories(user);

        StepVerifier.create(repositoryFlux)
                .expectNextMatches(repo -> {
                    try {
                        Repository expectedRepo = objectMapper.readValue(repos, Repository.class);
                        assertThat(repo).usingRecursiveComparison().isEqualTo(expectedRepo);
                        return true;
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyRepository_WhenUserExistsButHasNoRepo() throws URISyntaxException, IOException {
        String emptyRepo = Files
                .readString(Paths.get(Objects.requireNonNull(getClass().getResource("/empty-repo.json")).toURI()));

        String user = "huga721";

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/users/" + user + "/repos"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(emptyRepo)));

        Flux<Repository> repositoryFlux = githubClient.getUserRepositories(user);

        StepVerifier.create(repositoryFlux)
                .expectNextCount(0)
                .expectComplete()
                .verify();
    }

    @Test
    void shouldThrowException_WhenApiCantFindUser() {
        String user = "huga721";

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/users/" + user + "/repos"))
                .willReturn(aResponse()
                        .withStatus(404)));

        Flux<Repository> repositoryFlux = githubClient.getUserRepositories(user);

        StepVerifier.create(repositoryFlux)
                .expectError(GithubUserNotFoundException.class)
                .verify();
    }
}