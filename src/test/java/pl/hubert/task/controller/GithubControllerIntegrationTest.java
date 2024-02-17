package pl.hubert.task.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import pl.hubert.task.model.Branch;
import pl.hubert.task.model.Repository;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GithubControllerIntegrationTest {

    private WireMockServer mockServer;
    private WebTestClient webTestClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort().dynamicHttpsPort());
        mockServer.start();

        WireMock.configureFor("localhost", mockServer.port());

        webTestClient = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + mockServer.port() +"/api/github/")
                .build();
    }

    @AfterEach
    void tearDown() {
        mockServer.stop();
    }

    @Test
    void shouldReturnOneUserRepository() throws JsonProcessingException {
        Branch testBranch = new Branch("main", new Branch.Commit("sha123"));
        Repository testRepository = new Repository(new Repository.Owner("huga721"), "recruitment-task",
                false, List.of(testBranch));
        List<Repository> testRepositories = List.of(testRepository, testRepository);

        String reposMapped = objectMapper.writeValueAsString(testRepositories);

        String user = "huga721";

        WireMock.stubFor(WireMock.get(WireMock.urlMatching("/api/github/" + user))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(reposMapped)));

        webTestClient.get()
                .uri(user)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Repository.class)
                .hasSize(2)
                .value(repo -> {
                    try {
                        String resultRepo = objectMapper.writeValueAsString(repo.get(0));
                        String requestRepo = objectMapper.writeValueAsString(repo.get(0));
                        assertEquals(resultRepo.hashCode(), requestRepo.hashCode());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test
    void shouldReturnUserWithEmptyRepositories() {
        String user = "huga721";

        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/github/" + user))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));

        webTestClient.get()
                .uri(user)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Repository.class)
                .hasSize(0);
    }

    @Test
    void shouldThrowException_WhenApiCantFindUser() {
        String user = "huga721";

        mockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/api/github/" + user))
                .willReturn(aResponse()
                        .withStatus(404)));

        webTestClient.get()
                .uri(user)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}