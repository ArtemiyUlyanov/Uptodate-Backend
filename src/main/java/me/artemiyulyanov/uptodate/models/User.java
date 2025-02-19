package me.artemiyulyanov.uptodate.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    @JsonIgnore
    private String email, password;

    private String firstName, lastName, icon;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ArticleLike> likedArticles = new HashSet<>();

    @JsonIgnore // a new Uptodate data approaching update
    @ManyToMany(mappedBy = "likes")
    private Set<ArticleComment> likedComments = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;

    @JsonIgnore // a new Uptodate data approaching update
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Article> articles = new ArrayList<>();

    @JsonIgnore // a new Uptodate data approaching update
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArticleComment> comments = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserSettings settings;

    @PostLoad
    public void init() {
        if (settings == null) {
            this.settings = UserSettings.getDefaultSettings(this);
        }
    }

    @JsonIgnore
    @Transient
    public UserStatistics getStatistics() {
        return UserStatistics.builder()
                .user(this)
                .build();
    }

    public List<Long> getLikedCommentsIds() {
        return likedComments.stream()
                .map(ArticleComment::getId)
                .toList();
    }

    public List<Long> getArticlesIds() {
        return articles.stream()
                .map(Article::getId)
                .toList();
    }

    public List<Long> getCommentsIds() {
        return comments.stream()
                .map(ArticleComment::getId)
                .toList();
    }
}