package pl.hubert.task.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {

    @JsonProperty("owner_login")
    private String ownerLogin;

    @JsonProperty("repository_name")
    @JsonAlias("name")
    private String repositoryName;

    @JsonIgnore
    private boolean fork;

    private List<Branch> branches;

    public Repository(Repository repository, List<Branch> branches) {
        this.ownerLogin = repository.getOwnerLogin();
        this.repositoryName = repository.getRepositoryName();
        this.fork = repository.isFork();
        this.branches = branches;
    }

    @JsonProperty("owner")
    private void setOwnerLoginJsonProperty(Map<String,String> commit) {
        this.ownerLogin = commit.get("login");
    }
}
