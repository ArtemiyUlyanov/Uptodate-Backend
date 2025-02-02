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

import java.io.File;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public ResponseEntity<?> getCommentById(@RequestParam Long id, Model model) {
        Optional<ArticleComment> articleComment = articleCommentService.findById(id);

        if (articleComment.isEmpty()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "Comment is undefined!");
        }

        return requestService.executeEntityResponse(HttpStatus.OK, "The request has been proceeded successfully!", articleComment.get());
    }

    @GetMapping(value = "/get", params = {"authorId"})
    public ResponseEntity<?> getCommentsByAuthor(@RequestParam Long authorId, Model model) {
        Optional<User> wrappedAuthor = userService.findById(authorId);

        if (!wrappedAuthor.isPresent()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "Author is undefined!");
        }

        List<ArticleComment> articleComments = articleCommentService.findByAuthor(wrappedAuthor.get());
        return requestService.executeEntityResponse(HttpStatus.OK, "The request has been proceeded successfully!", articleComments);
    }

    @GetMapping(value = "/get", params = {"articleId"})
    public ResponseEntity<?> getCommentsByArticle(@RequestParam Long articleId, Model model) {
        Optional<Article> wrappedArticle = articleService.findById(articleId);

        if (!wrappedArticle.isPresent()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "Comment is undefined!");
        }

        List<ArticleComment> articleComments = articleCommentService.findByArticle(wrappedArticle.get());
        return requestService.executeEntityResponse(HttpStatus.OK, "The request has been proceeded successfully!", articleComments);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createComment(
            @RequestParam String content,
            @RequestParam Long articleId,
            @RequestParam(value = "resources", required = false) List<MultipartFile> resources,
            Model model
    ) {
        Optional<User> wrappedUser = getAuthorizedUser();
        Optional<Article> wrappedArticle = articleService.findById(articleId);

        if (wrappedArticle.isEmpty()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "The article is undefined");
        }

        ArticleComment comment = new ArticleComment();

        comment.setCreatedAt(LocalDateTime.now());
        comment.setContent(content);
        comment.setArticle(wrappedArticle.get());
        comment.setAuthor(wrappedUser.get());
        articleCommentService.save(comment);

        articleCommentService.getResourceManager().uploadResources(comment, resources);

        return requestService.executeApiResponse(HttpStatus.OK, "The comment has been created!");
    }

//    @PostMapping("/create")
//    public ResponseEntity<?> createComment(@RequestBody ArticleComment comment, @RequestParam(value = "resources", required = false) List<MultipartFile> resources, Model model) {
//        Optional<User> wrappedUser = getAuthorizedUser();
//
////        if (!isUserAuthorized()) {
////            return requestService.executeApiResponse(HttpStatus.UNAUTHORIZED, "The authorized user is undefined!");
////        }
//
//        comment.setCreatedAt(LocalDateTime.now());
//        comment.setAuthor(wrappedUser.get());
//
//        articleCommentService.save(comment);
//        if (resources != null) {
//            articleCommentService.getResourceManager().uploadResources(comment, resources);
//        }
//
//        return requestService.executeApiResponse(HttpStatus.OK, "The comment has been created!");
//    }

    @PutMapping("/edit")
    public ResponseEntity<?> editComment(
            @RequestParam Long id,
            @RequestParam String content,
            @RequestParam(value = "resources", required = false) List<MultipartFile> resources,
            Model model
    ) {
        Optional<User> wrappedUser = getAuthorizedUser();
        Optional<ArticleComment> wrappedArticleComment = articleCommentService.findById(id);

//        if (!isUserAuthorized()) {
//            return requestService.executeApiResponse(HttpStatus.UNAUTHORIZED, "The authorized user is undefined!");
//        }

        if (wrappedArticleComment.isEmpty()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "Comment is undefined!");
        }

        ArticleComment newArticleComment = wrappedArticleComment.get();
        if (!newArticleComment.getAuthor().getId().equals(wrappedUser.get().getId())) {
            return requestService.executeApiResponse(HttpStatus.FORBIDDEN, "The authorized user has no authority to proceed the changes!");
        }

//        updates.forEach((key, value) -> {
//            Field field = ReflectionUtils.findField(Article.class, key);
//            if (field != null) {
//                field.setAccessible(true);
//                ReflectionUtils.setField(field, newArticleComment, value);
//            }
//        });

        newArticleComment.setContent(content);

        articleCommentService.save(newArticleComment);
        articleCommentService.getResourceManager().updateResources(newArticleComment, resources);

        return requestService.executeApiResponse(HttpStatus.OK, "The changes have been applied successfully!");
    }

    @PostMapping("/like")
    public ResponseEntity<?> likeComment(@RequestParam Long id, Model model) {
        Optional<User> wrappedUser = getAuthorizedUser();

//        if (!isUserAuthorized()) {
//            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, 10, "The authorized user is undefined!");
//        }

        Optional<ArticleComment> wrappedArticleComment = articleCommentService.findById(id);
        if (wrappedArticleComment.isEmpty()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "The comment is undefined!");
        }

        ArticleComment newArticleComment = wrappedArticleComment.get();
        newArticleComment.like(wrappedUser.get());
        articleCommentService.save(newArticleComment);

        return requestService.executeApiResponse(HttpStatus.OK, "The comment has been liked by the user successfully!");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteComment(@RequestParam Long id, Model model) {
        Optional<User> wrappedUser = getAuthorizedUser();
        Optional<ArticleComment> wrappedArticleComment = articleCommentService.findById(id);

//        if (!isUserAuthorized()) {
//            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "The authorized user is undefined!");
//        }

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
}