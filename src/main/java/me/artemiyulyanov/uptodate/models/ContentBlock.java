package me.artemiyulyanov.uptodate.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "articles_content_blocks")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContentBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    private String type, text;

    public Long getArticleId() {
        return article.getId();
    }
}
