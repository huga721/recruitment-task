package pl.hubert.task.controller;

import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pl.hubert.task.client.GithubClient;
import pl.hubert.task.exception.ExceptionMessage;
import pl.hubert.task.model.Branch;
import pl.hubert.task.model.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;


@ExtendWith(SpringExtension.class)
@WebFluxTest(GithubController.class)
@Import(GithubClient.class)
class GithubControllerIntegrationTest {

    private MockWebServer mockWebServer;

    @MockBean
    private GithubClient githubClient;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        mockWebServer = new MockWebServer();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void shouldReturnUserRepositories() {
        Branch branch = new Branch("main", "sha123");
        Repository repository = new Repository("huga721", "recruitment-task", false, List.of(branch));

        Mockito.when(githubClient.getUserRepositories(anyString()))
                        .thenReturn(Flux.just(repository));

        webTestClient.get().uri("/api/github/huga721")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Repository.class)
                .hasSize(1)
                .value(repos -> {
                    Repository foundRepo = repos.get(0);
                    assertThat(foundRepo.getRepositoryName()).isEqualTo("recruitment-task");
                    assertThat(foundRepo.getOwnerLogin()).isEqualTo("huga721");
                    assertThat(foundRepo.getBranches()).hasSize(1);
                    Branch foundBranch = foundRepo.getBranches().get(0);
                    assertThat(foundBranch.getName()).isEqualTo("main");
                    assertThat(foundBranch.getLastCommitSha()).isEqualTo("sha123");
                });
    }

    @Test
    void shouldNotReturnUserRepositories_WhenUserNotExist() {
        Flux<Repository> repositoryFlux = Flux.error(new WebClientResponseException(HttpStatus.NOT_FOUND.value(), "Not Found", null, null, null));

        Mockito.when(githubClient.getUserRepositories(anyString()))
                .thenReturn(repositoryFlux);

        webTestClient.get().uri("/api/github/notexist")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ExceptionMessage.class);
    }
}