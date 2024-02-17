package pl.hubert.task.client;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.hubert.task.exception.model.GithubUserNotFoundException;
import pl.hubert.task.model.Branch;
import pl.hubert.task.model.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class GithubClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubClient.class);

    private final WebClient githubWebClient;

    public Flux<Repository> getUserRepositories(String username) {
        LOGGER.info("Fetching repositories for user: {}", username);

        return githubWebClient.get()
                .uri(("/users/{username}/repos"), username)
                .retrieve()
                .onStatus(httpStatusCode -> httpStatusCode.value() == 404,
                        error -> Mono.error(new GithubUserNotFoundException("Github api did not find user: " + username)))
                .bodyToFlux(Repository.class)
                .flatMap(this::fetchBranchesForRepository)
                .filter(repository -> !repository.fork());
    }

        private Mono<Repository> fetchBranchesForRepository(Repository repo) {
        LOGGER.info("Fetching branches for repository: {}/{}", repo.owner().login(), repo.repositoryName());

        return githubWebClient.get()
                .uri(("/repos/{login}/{repositoryName}/branches"), repo.owner().login(), repo.repositoryName())
                .retrieve()
                .bodyToFlux(Branch.class)
                .collectList()
                .flatMap(branches -> Mono.just(new Repository(repo, branches)));
    }
}
