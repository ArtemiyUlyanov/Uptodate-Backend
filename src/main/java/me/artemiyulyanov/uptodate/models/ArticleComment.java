package me.artemiyulyanov.uptodate.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.artemiyulyanov.uptodate.services.ArticleCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "articles_comments")
@AllArgsConstructor
@NoArgsConstructor
public class ArticleComment {
    @Setter
    private static ArticleCommentService articleCommentService;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    @JsonIgnoreProperties({"author", "comments"})
    private Article article;

//    @ElementCollection
//    @CollectionTable(name = "comments_resources", joinColumns = @JoinColumn(name = "comment_id"))
//    @Column(name = "resource")
//    private List<String> resources;

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
    @JsonIgnoreProperties({"articles", "comments", "likedArticles", "likedComments"})
    private Set<User> articleCommentLikes = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"articles", "comments", "likedArticles", "likedComments"})
    private User author;

    @PostLoad
    private void initResources() {
        if (articleCommentService != null) {
            this.resources = articleCommentService.getResourceManager().getResources(this);
        }
    }

    public List<String> getLikedUsernames() {
        return articleCommentLikes.stream().map(User::getUsername).toList();
    }

    @Transient
    public ArticleComment like(User user) {
        if (!articleCommentLikes.contains(user)) {
            articleCommentLikes.add(user);
        } else {
            articleCommentLikes.remove(user);
        }

        return this;
    }
}