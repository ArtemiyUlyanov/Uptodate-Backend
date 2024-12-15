package me.artemiyulyanov.uptodate.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "articles_comments")
@AllArgsConstructor
@NoArgsConstructor
public class ArticleComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    @JsonIgnoreProperties({"author", "comments"})
    @Getter
    @Setter
    private Article article;

    @Getter
    @Setter
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Getter
    @Setter
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"articles", "comments"})
    @Getter
    @Setter
    private User author;
}