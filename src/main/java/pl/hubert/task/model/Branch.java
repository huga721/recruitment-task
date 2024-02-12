package pl.hubert.task.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Branch {

    private String name;

    @JsonProperty("last_commit_sha")
    private String lastCommitSha;

    @JsonProperty("commit")
    private void setLastCommitShaJsonProperty(Map<String,String> branchInfo) {
        this.lastCommitSha = branchInfo.get("sha");
    }
}