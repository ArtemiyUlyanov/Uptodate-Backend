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
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/comments")
public class ArticleCommentController extends AuthenticatedController {
    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleCommentService articleCommentService;

    @Autowired
    private RequestService requestService;

    @GetMapping
    public ResponseEntity<?> getCommentsByIds(@RequestParam(defaultValue = "", required = false) List<Long> ids) {
        List<ArticleComment> comments = articleCommentService.findAllById(ids);
        return requestService.executeEntityResponse(HttpStatus.OK, "The comments have been retrieved successfully!", comments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCommentById(@PathVariable Long id) {
        Optional<ArticleComment> articleComment = articleCommentService.findById(id);

        if (articleComment.isEmpty()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "Comment is undefined!");
        }

        return requestService.executeEntityResponse(HttpStatus.OK, "The comment has been retrieved successfully!", articleComment.get());
    }

    @GetMapping("/author/{id}")
    public ResponseEntity<?> getCommentsByAuthor(@PathVariable Long id) {
        Optional<User> wrappedAuthor = userService.findById(id);

        if (!wrappedAuthor.isPresent()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "Author is undefined!");
        }

        List<ArticleComment> articleComments = articleCommentService.findByAuthor(wrappedAuthor.get());
        return requestService.executeEntityResponse(HttpStatus.OK, "The request has been proceeded successfully!", articleComments);
    }

    @GetMapping("/article/{id}")
    public ResponseEntity<?> getCommentsByArticle(@PathVariable Long id) {
        Optional<Article> wrappedArticle = articleService.findById(id);

        if (!wrappedArticle.isPresent()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "Comment is undefined!");
        }

        List<ArticleComment> articleComments = articleCommentService.findByArticle(wrappedArticle.get());
        return requestService.executeEntityResponse(HttpStatus.OK, "The request has been proceeded successfully!", articleComments);
    }

    @PostMapping
    public ResponseEntity<?> createComment(
            @RequestParam String content,
            @RequestParam Long articleId,
            @RequestParam(value = "resources", required = false) List<MultipartFile> resources) {
        Optional<User> wrappedUser = getAuthorizedUser();
        Optional<Article> wrappedArticle = articleService.findById(articleId);

        if (wrappedArticle.isEmpty()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "The article is undefined");
        }

        articleCommentService.create(content, wrappedUser.get(), wrappedArticle.get(), resources);
        return requestService.executeApiResponse(HttpStatus.OK, "The comment has been created!");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editComment(
            @PathVariable Long id,
            @RequestParam String content,
            @RequestParam(value = "resources", required = false) List<MultipartFile> resources) {
        Optional<User> wrappedUser = getAuthorizedUser();
        Optional<ArticleComment> wrappedArticleComment = articleCommentService.findById(id);

        if (wrappedArticleComment.isEmpty()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "Comment is undefined!");
        }

        ArticleComment newArticleComment = wrappedArticleComment.get();
        if (!newArticleComment.getAuthor().getId().equals(wrappedUser.get().getId())) {
            return requestService.executeApiResponse(HttpStatus.FORBIDDEN, "The authorized user has no authority to proceed the changes!");
        }

        articleCommentService.edit(id, content, resources);
        return requestService.executeApiResponse(HttpStatus.OK, "The changes have been applied successfully!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id) {
        Optional<User> wrappedUser = getAuthorizedUser();
        Optional<ArticleComment> wrappedArticleComment = articleCommentService.findById(id);

        if (wrappedArticleComment.isEmpty()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "Comment is undefined!");
        }

        ArticleComment comment = wrappedArticleComment.get();
        if (!comment.getAuthor().getId().equals(wrappedUser.get().getId())) {
            return requestService.executeApiResponse(HttpStatus.FORBIDDEN, "The authorized user has no authority to proceed the removal!");
        }

        articleCommentService.delete(comment);
        return requestService.executeApiResponse(HttpStatus.OK, "The removal has been processed successfully!");
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeComment(@PathVariable Long id) {
        Optional<User> wrappedUser = getAuthorizedUser();

        Optional<ArticleComment> wrappedArticleComment = articleCommentService.findById(id);
        if (wrappedArticleComment.isEmpty()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "The comment is undefined!");
        }

        ArticleComment newArticleComment = wrappedArticleComment.get();
        newArticleComment.like(wrappedUser.get());
        articleCommentService.save(newArticleComment);

        return requestService.executeApiResponse(HttpStatus.OK, "The comment has been liked by the user successfully!");
    }
}