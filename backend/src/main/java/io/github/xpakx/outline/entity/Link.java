package io.github.xpakx.outline.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String longUrl;
    private String title;
    private String author;
    private String date;

    @Column(columnDefinition="TEXT")
    private String content;
}
