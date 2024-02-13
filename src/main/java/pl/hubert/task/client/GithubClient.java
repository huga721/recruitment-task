package pl.hubert.task.client;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.hubert.task.model.Branch;
import pl.hubert.task.model.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class GithubClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubClient.class);

    private final WebClient webClient;

    public Flux<Repository> getUserRepositories(String username) {
        LOGGER.info("Fetching repositories for user: {}", username);

        return webClient.get()
                .uri(("/users/{username}/repos"), username)
                .retrieve()
                .bodyToFlux(Repository.class)
                .flatMap(this::fetchBranchesForRepository)
                .filter(repository -> !repository.isFork());
    }

    private Mono<Repository> fetchBranchesForRepository(Repository repo) {
        LOGGER.info("Fetching branches for repository: {}/{}", repo.getOwnerLogin(), repo.getRepositoryName());

        return webClient.get()
                .uri(("/repos/{ownerLogin}/{repositoryName}/branches"), repo.getOwnerLogin(), repo.getRepositoryName())
                .retrieve()
                .bodyToFlux(Branch.class)
                .collectList()
                .flatMap(branches -> Mono.just(new Repository(repo, branches)));
    }
}
