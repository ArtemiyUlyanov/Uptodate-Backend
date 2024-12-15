package me.artemiyulyanov.uptodate.controllers.api.articles;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import me.artemiyulyanov.uptodate.controllers.AuthenticatedController;
import me.artemiyulyanov.uptodate.models.Article;
import me.artemiyulyanov.uptodate.models.ArticleComment;
import me.artemiyulyanov.uptodate.models.ArticleTopic;
import me.artemiyulyanov.uptodate.models.User;
import me.artemiyulyanov.uptodate.services.ArticleCommentService;
import me.artemiyulyanov.uptodate.services.ArticleService;
import me.artemiyulyanov.uptodate.services.ArticleTopicService;
import me.artemiyulyanov.uptodate.services.UserService;
import me.artemiyulyanov.uptodate.web.RequestService;
import me.artemiyulyanov.uptodate.web.ServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/articles/comments")
public class ArticleCommentController extends AuthenticatedController {
    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleCommentService articleCommentService;

    @Autowired
    private RequestService requestService;

    @GetMapping(value = "/get", params = {"id"})
    public ResponseEntity<ServerResponse> getArticleCommentById(@RequestParam Long id, Model model) {
        Optional<ArticleComment> articleComment = articleCommentService.findById(id);

        if (!articleComment.isPresent()) {
            return requestService.executeError(HttpStatus.BAD_REQUEST, 10, "Article comment is undefined!");
        }

        return requestService.executeEntity(HttpStatus.OK, 200, "The request has been proceeded successfully!", articleComment.get());
    }

    @GetMapping(value = "/get", params = {"authorId"})
    public ResponseEntity<ServerResponse> getArticleCommentsByAuthor(@RequestParam Long authorId, Model model) {
        Optional<User> wrappedAuthor = userService.findById(authorId);

        if (!wrappedAuthor.isPresent()) {
            return requestService.executeError(HttpStatus.BAD_REQUEST, 10, "Author is undefined!");
        }

        List<ArticleComment> articleComments = articleCommentService.findByAuthor(wrappedAuthor.get());
        return requestService.executeEntity(HttpStatus.OK, 200, "The request has been proceeded successfully!", articleComments);
    }

    @GetMapping(value = "/get", params = {"articleId"})
    public ResponseEntity<ServerResponse> getArticleCommentsByArticle(@RequestParam Long articleId, Model model) {
        Optional<Article> wrappedArticle = articleService.findById(articleId);

        if (!wrappedArticle.isPresent()) {
            return requestService.executeError(HttpStatus.BAD_REQUEST, 10, "Article is undefined!");
        }

        List<ArticleComment> articleComments = articleCommentService.findByArticle(wrappedArticle.get());
        return requestService.executeEntity(HttpStatus.OK, 200, "The request has been proceeded successfully!", articleComments);
    }

    @PostMapping("/create")
    public ResponseEntity<ServerResponse> createArticleComment(@RequestBody ArticleComment comment, @RequestParam(value = "resources", required = false) List<MultipartFile> resources, Model model) {
        Optional<User> wrappedUser = getAuthorizedUser();

        if (!isUserAuthorized()) {
            return requestService.executeError(HttpStatus.BAD_REQUEST, 10, "The authorized user is undefined!");
        }

        comment.setCreatedAt(LocalDateTime.now());
        comment.setAuthor(wrappedUser.get());

        articleCommentService.save(comment);
        if (resources != null) articleCommentService.loadResources(comment, resources);

        return requestService.executeMessage(HttpStatus.OK, 200, "The article has been created!");
    }

    @PatchMapping("/edit")
    public ResponseEntity<ServerResponse> editArticle(@RequestParam Long id, @RequestBody Map<String, Object> updates, @RequestParam(value = "newFiles", required = false) List<MultipartFile> newFiles, Model model) {
        Optional<User> wrappedUser = getAuthorizedUser();
        Optional<ArticleComment> wrappedArticleComment = articleCommentService.findById(id);

        if (!isUserAuthorized()) {
            return requestService.executeError(HttpStatus.BAD_REQUEST, 10, "The authorized user is undefined!");
        }

        if (!wrappedArticleComment.isPresent()) {
            return requestService.executeError(HttpStatus.BAD_REQUEST, 20, "Article comment is undefined!");
        }

        ArticleComment newArticleComment = wrappedArticleComment.get();
        if (!newArticleComment.getAuthor().getId().equals(wrappedUser.get().getId())) {
            return requestService.executeError(HttpStatus.BAD_REQUEST, 11, "The authorized user has no authority to proceed the changes!");
        }

        updates.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(Article.class, key);
            if (field != null) {
                field.setAccessible(true);
                ReflectionUtils.setField(field, newArticleComment, value);
            }
        });

        articleCommentService.save(newArticleComment);
        if (newFiles != null) articleCommentService.loadResources(newArticleComment, newFiles);

        return requestService.executeMessage(HttpStatus.OK, 200, "The changes have been applied successfully!");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ServerResponse> deleteArticle(@RequestParam Long id, Model model) {
        Optional<User> wrappedUser = getAuthorizedUser();
        Optional<ArticleComment> wrappedArticleComment = articleCommentService.findById(id);

        if (!isUserAuthorized()) {
            return requestService.executeError(HttpStatus.BAD_REQUEST, 10, "The authorized user is undefined!");
        }

        if (!wrappedArticleComment.isPresent()) {
            return requestService.executeError(HttpStatus.BAD_REQUEST, 20, "Article is undefined!");
        }

        ArticleComment comment = wrappedArticleComment.get();
        if (!comment.getAuthor().getId().equals(wrappedUser.get().getId())) {
            return requestService.executeError(HttpStatus.BAD_REQUEST, 11, "The authorized user has no authority to proceed the removal!");
        }

        articleCommentService.delete(comment);
        return requestService.executeMessage(HttpStatus.OK, 200, "The removal has been processed successfully!");
    }
}