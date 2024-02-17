package pl.hubert.task.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record Repository(
        Owner owner,
        @JsonProperty("repository_name")
        @JsonAlias("name")
        String repositoryName,
        @JsonIgnore
        boolean fork,
        List<Branch> branches) {

    public Repository(Repository repo, List<Branch> branches) {
        this(repo.owner, repo.repositoryName, repo.fork, branches);
    }
    public record Owner(String login) {
    }
}
