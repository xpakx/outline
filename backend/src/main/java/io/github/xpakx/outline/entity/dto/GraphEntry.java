package io.github.xpakx.outline.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GraphEntry {
    @JsonProperty("@type")
    String type;
    String name;
}
