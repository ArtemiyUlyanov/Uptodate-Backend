package me.artemiyulyanov.uptodate.controllers.api.articles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import me.artemiyulyanov.uptodate.controllers.AuthenticatedController;
import me.artemiyulyanov.uptodate.controllers.api.articles.filters.ArticleFilter;
import me.artemiyulyanov.uptodate.models.Article;
import me.artemiyulyanov.uptodate.models.ArticleTopic;
import me.artemiyulyanov.uptodate.models.User;
import me.artemiyulyanov.uptodate.services.*;
import me.artemiyulyanov.uptodate.web.PageableObject;
import me.artemiyulyanov.uptodate.web.RequestService;
import me.artemiyulyanov.uptodate.web.ServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/articles")
public class ArticleController extends AuthenticatedController {
    public static final int ARTICLE_PAGE_SIZE = 2;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleTopicService articleTopicService;

    @Autowired
    private ArticleViewService articleViewService;

    @Autowired
    private ArticleLikeService articleLikeService;

    @Autowired
    private UserService userService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping(value = "/get", params = {"id"})
    public ResponseEntity<?> getArticleById(@RequestParam Long id) {
        Optional<Article> wrappedArticle = articleService.findById(id);

        if (wrappedArticle.isEmpty()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "Article is undefined!");
        }

        return requestService.executeEntityResponse(HttpStatus.OK, "The request has been proceeded successfully!", wrappedArticle.get());
    }

    @GetMapping(value = "/get", params = {"authorId"})
    public ResponseEntity<?> getArticlesByAuthor(@RequestParam Long authorId) {
        Optional<User> wrappedAuthor = userService.findById(authorId);

        if (wrappedAuthor.isEmpty()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "User is undefined!");
        }

        List<Article> articles = articleService.findByAuthor(wrappedAuthor.get());
        return requestService.executeEntityResponse(HttpStatus.OK, "The request has been proceeded successfully!", articles);
    }

    @GetMapping("/retrieve")
    public ResponseEntity<?> retrieveArticle(
            @RequestParam(value = "heading") String heading,
            @RequestParam(value = "createdAt") String createdAtString) {
        Date date;

        try {
            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(createdAtString);
        } catch (ParseException e) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "The date is wrong!");
        }

        Optional<Article> wrappedArticle = articleService.findByDateAndHeadingContaining(date, heading);

        if (wrappedArticle.isEmpty()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "Article is undefined!");
        }

        articleViewService.view(wrappedArticle.get(), getAuthorizedUser().orElse(null));
        return requestService.executeEntityResponse(HttpStatus.OK, "The request has been proceeded successfully!", wrappedArticle.get());
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchArticles(
            @RequestParam(defaultValue = "1", required = false) Integer page,
            @RequestParam(required = false) Integer pagesCount,
            @RequestParam(required = false) Integer count,
            @RequestParam String query,
            @RequestParam(value = "filters") String filtersRow) throws JsonProcessingException, UnsupportedEncodingException {
        HashMap<String, Object> filters = objectMapper.readValue(URLDecoder.decode(filtersRow, "UTF-8"), new TypeReference<>() {});
        filters.put("query", query);

        PageableObject<Article> pageableObject;

        if (count != null) {
            pageableObject = PageableObject.of(Article.class, 0, count);
        } else if (pagesCount != null) {
            pageableObject = PageableObject.of(Article.class, 0, pagesCount * ARTICLE_PAGE_SIZE);
        } else {
            pageableObject = PageableObject.of(Article.class, page - 1, ARTICLE_PAGE_SIZE);
        }

        Page<Article> paginatedArticles = articleService.findAllArticles(
                ArticleFilter.applyFilters(
                        pageableObject,
                        filters
                )
        );

        return requestService.executePaginatedEntityResponse(HttpStatus.OK, paginatedArticles);
    }

    @PostMapping("/like")
    public ResponseEntity<?> likeArticle(@RequestParam Long id, Model model) {
        Optional<User> wrappedUser = getAuthorizedUser();

        Optional<Article> wrappedArticle = articleService.findById(id);
        if (wrappedArticle.isEmpty()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "The article is undefined!");
        }

        articleLikeService.like(wrappedArticle.get(), wrappedUser.get());
        return requestService.executeApiResponse(HttpStatus.OK, "The article has been liked by the user successfully!");
    }

    @PostMapping("/create")
    public ResponseEntity<?> createArticle(
            @RequestParam String heading,
            @RequestParam String description,
            @RequestParam String content,
            @RequestParam List<String> topicsNames,
            @RequestParam(value = "resources", required = false) List<MultipartFile> resources) {
        Optional<User> wrappedUser = getAuthorizedUser();

        Set<ArticleTopic> topics = topicsNames.stream()
                        .map(articleTopicService::findByName)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet());

        Article article = Article.builder()
                        .heading(heading)
                        .description(description)
                        .content(content)
                        .topics(topics)
                        .createdAt(LocalDateTime.now())
                        .author(wrappedUser.get())
                        .build();

        articleService.save(article);
        if (resources != null) {
            articleService.getResourceManager().uploadResources(article, resources);
        }

        return requestService.executeApiResponse(HttpStatus.OK, "The article has been created!");
    }

    @PutMapping("/edit")
    public ResponseEntity<?> editArticle(
            @RequestParam Long id,
            @RequestParam String heading,
            @RequestParam String description,
            @RequestParam String content,
            @RequestParam List<String> topicsNames,
            @RequestParam(value = "newFiles", required = false) List<MultipartFile> newFiles) {
        Optional<User> wrappedUser = getAuthorizedUser();
        Optional<Article> wrappedArticle = articleService.findById(id);

        if (wrappedArticle.isEmpty()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "Article is undefined!");
        }

        Article newArticle = wrappedArticle.get();
        if (!newArticle.getAuthor().getId().equals(wrappedUser.get().getId())) {
            return requestService.executeApiResponse(HttpStatus.FORBIDDEN, "The authorized user has no authority to proceed the changes!");
        }

        articleService.editArticle(id, heading, description, content, topicsNames, newFiles);
        return requestService.executeApiResponse(HttpStatus.OK, "The changes have been applied successfully!");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteArticle(@RequestParam Long id) {
        Optional<User> wrappedUser = getAuthorizedUser();
        Optional<Article> wrappedArticle = articleService.findById(id);

//        if (!isUserAuthorized()) {
//            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, 10, "The authorized user is undefined!");
//        }

        if (wrappedArticle.isEmpty()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "Article is undefined!");
        }

        Article article = wrappedArticle.get();
        if (!article.getAuthor().getId().equals(wrappedUser.get().getId())) {
            return requestService.executeApiResponse(HttpStatus.FORBIDDEN, "The authorized user has no authority to proceed the removal!");
        }

        articleService.delete(article);
        return requestService.executeApiResponse(HttpStatus.OK, "The removal has been processed successfully!");
    }
}