package me.artemiyulyanov.uptodate.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import me.artemiyulyanov.uptodate.repositories.UserRepository;
import me.artemiyulyanov.uptodate.services.UserService;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "articles")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Article {
    @Setter
    private static UserService userService;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String heading, description, cover;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContentBlock> content;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArticleView> views = new ArrayList<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArticleLike> likes = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "articles_categories",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Transient
    private List<PermissionScope> permissionScope = new ArrayList<>();

    @PostLoad
    public void init() {
        if (userService != null) {
            User wrappedUser = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).orElse(null);
            this.permissionScope = definePermissionScopeFor(this, wrappedUser);
        }
    }

    public List<String> getLikedUsernames() {
        return likes.stream()
                .map(ArticleLike::getUser)
                .map(User::getUsername)
                .toList();
    }

    public List<Long> getCommentsIds() {
        return comments.stream()
                .map(Comment::getId)
                .toList();
    }

    public Long getAuthorId() {
        return author.getId();
    }

    public int getLikesCount() {
        return likes.size();
    }

    public int getViewsCount() {
        return views.size();
    }

    public static List<PermissionScope> definePermissionScopeFor(Article article, User user) {
        if (user == null) return Collections.emptyList();
        if (user.getRolesNames().contains("ADMIN") || user.getId() == article.getAuthor().getId()) return List.of(PermissionScope.EDIT, PermissionScope.DELETE);

        return Collections.emptyList();
    }
}