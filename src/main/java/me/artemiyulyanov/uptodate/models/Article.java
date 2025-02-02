package me.artemiyulyanov.uptodate.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "articles")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer views = 0;

    @ManyToMany
    @JoinTable(
            name = "articles_likes",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties({"articles", "comments", "likedArticles", "likedComments"})
    private Set<User> articleLikes = new HashSet<>();

    private String heading, description, content;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArticleComment> comments = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "articles_topics",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    private Set<ArticleTopic> topics = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"articles", "comments", "likedArticles", "likedComments"})
    private User author;

    public List<String> getLikedUsernames() {
        return articleLikes.stream().map(User::getUsername).toList();
    }

    @Transient
    public Article like(User user) {
        if (!articleLikes.contains(user)) {
            articleLikes.add(user);
        } else {
            articleLikes.remove(user);
        }

        return this;
    }

    @Transient
    public Article like(User user, boolean liked) {
        if (liked && !articleLikes.contains(user)) {
            articleLikes.add(user);
        } else if (!liked && articleLikes.contains(user)) {
            articleLikes.remove(user);
        }

        return this;
    }
}