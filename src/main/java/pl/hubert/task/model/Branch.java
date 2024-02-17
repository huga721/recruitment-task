package pl.hubert.task.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Branch(
        String name,
        Commit commit) {

    public record Commit(String sha) {
    }
}