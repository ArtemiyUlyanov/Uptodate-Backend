package me.artemiyulyanov.uptodate.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import me.artemiyulyanov.uptodate.services.ArticleCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "articles_comments")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleComment {
    @Setter
    private static ArticleCommentService articleCommentService;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Transient
    private List<String> resources;

    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(
            name = "article_comments_likes",
            joinColumns = @JoinColumn(name = "comment_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private Set<User> likes = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User author;

    @PostLoad
    private void initResources() {
        if (articleCommentService != null) {
            this.resources = articleCommentService.getResourceManager().getResources(this);
        }
    }

    public Long getArticleId() {
        return article.getId();
    }

    public Long getAuthorId() {
        return author.getId();
    }

    public List<Long> getLikedUsersIds() {
        return likes.stream()
                .map(User::getId)
                .toList();
    }

    public List<String> getLikedUsernames() {
        return likes.stream()
                .map(User::getUsername)
                .toList();
    }

    @Transient
    public ArticleComment like(User user) {
        if (!likes.contains(user)) {
            likes.add(user);
        } else {
            likes.remove(user);
        }

        return this;
    }
}