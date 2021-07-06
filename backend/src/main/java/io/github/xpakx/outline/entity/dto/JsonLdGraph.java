package io.github.xpakx.outline.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class JsonLdGraph {
    @JsonProperty("@graph")
    List<GraphEntry> graph;
}
