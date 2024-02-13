package pl.hubert.task.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.hubert.task.model.Repository;
import pl.hubert.task.client.GithubClient;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/github")
public class GithubController {

    private final GithubClient githubService;

    @GetMapping(path = "/{username}")
    public Flux<Repository> getGithubReposByUsername(@PathVariable String username) {
        return githubService.getUserRepositories(username);
    }
}