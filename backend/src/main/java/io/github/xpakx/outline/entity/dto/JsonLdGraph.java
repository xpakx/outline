package io.github.xpakx.outline.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JsonLdGraph {
    @JsonProperty("@graph")
    List<GraphEntry> graph;
}
