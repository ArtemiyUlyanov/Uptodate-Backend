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
import me.artemiyulyanov.uptodate.controllers.AuthenticatedController;
import me.artemiyulyanov.uptodate.controllers.api.articles.filters.ArticleFilter;
import me.artemiyulyanov.uptodate.models.Article;
import me.artemiyulyanov.uptodate.models.ArticleTopic;
import me.artemiyulyanov.uptodate.models.User;
import me.artemiyulyanov.uptodate.web.PageableObject;
import me.artemiyulyanov.uptodate.web.RequestService;
import me.artemiyulyanov.uptodate.web.ServerResponse;
import me.artemiyulyanov.uptodate.services.ArticleService;
import me.artemiyulyanov.uptodate.services.ArticleTopicService;
import me.artemiyulyanov.uptodate.services.UserService;
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

@RestController
@RequestMapping("/api/articles")
@Tag(name = "Articles", description = "Endpoints for managing articles")
public class ArticleController extends AuthenticatedController {
    public static final int ARTICLE_PAGE_SIZE = 2;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleTopicService articleTopicService;

    @Autowired
    private UserService userService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping(value = "/get", params = {"id"})
    @Operation(summary = "Find articles with a query", description = "Provide conditions to look up specific article")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "The request has been proceeded successfully"),
        @ApiResponse(responseCode = "10", description = "User is undefined"),
        @ApiResponse(responseCode = "20", description = "Article is undefined")
    })
    public ResponseEntity<?> getArticleById(@Parameter(description = "An ID of article to find a matching article", required = true) @RequestParam Long id, Model model) {
        Optional<Article> wrappedArticle = articleService.findById(id);

        if (wrappedArticle.isEmpty()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "Article is undefined!");
        }

        return requestService.executeEntityResponse(HttpStatus.OK, "The request has been proceeded successfully!", wrappedArticle.get());
    }

    @GetMapping(value = "/get", params = {"authorId"})
    public ResponseEntity<?> getArticlesByAuthor(@Parameter(description = "An ID of author to find matching articles", required = true) @RequestParam Long authorId, Model model) {
        Optional<User> wrappedAuthor = userService.findById(authorId);

        if (wrappedAuthor.isEmpty()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "User is undefined!");
        }

        List<Article> articles = articleService.findByAuthor(wrappedAuthor.get());
        return requestService.executeEntityResponse(HttpStatus.OK, "The request has been proceeded successfully!", articles);
    }

    @GetMapping(value = "/get", params = {"heading", "createdAt"})
    public ResponseEntity<?> getArticleByHeadingAndDate(@RequestParam(value = "heading") String heading, @RequestParam(value = "createdAt") String createdAtString, Model model) {
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

        return requestService.executeEntityResponse(HttpStatus.OK, "The request has been proceeded successfully!", wrappedArticle.get());
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchArticles(@Parameter(description = "A page of paginated data") @RequestParam(defaultValue = "1", required = false) Integer page, @RequestParam(required = false) Integer pagesCount, @RequestParam(required = false) Integer count, @RequestParam String query, @RequestParam(value = "filters") String filtersRow, Model model) throws JsonProcessingException, UnsupportedEncodingException {
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

//        if (!isUserAuthorized()) {
//            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, 10, "The authorized user is undefined!");
//        }

        Optional<Article> wrappedArticle = articleService.findById(id);
        if (wrappedArticle.isEmpty()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "The article is undefined!");
        }

        Article newArticle = wrappedArticle.get();
        newArticle.like(wrappedUser.get());
        articleService.save(newArticle);
        return requestService.executeApiResponse(HttpStatus.OK, "The article has been liked by the user successfully!");
    }

    @PostMapping("/create")
    @Operation(summary = "Create a new article", description = "Provide article data to create a new article", responses = {
            @ApiResponse(responseCode = "200", description = "The request has been proceeded successfully"),
            @ApiResponse(responseCode = "10", description = "The authorized user is undefined")
    })
    public ResponseEntity<?> createArticle(@Schema(implementation = Article.class, description = "An article to be saved") @RequestBody Article article, @RequestParam(value = "resources", required = false) List<MultipartFile> resources, Model model) {
        Optional<User> wrappedUser = getAuthorizedUser();

//        if (!isUserAuthorized()) {
//            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, 10, "The authorized user is undefined!");
//        }

        article.setCreatedAt(LocalDateTime.now());
        article.setAuthor(wrappedUser.get());

        articleService.save(article);
        if (resources != null) {
            articleService.getResourceManager().uploadResources(article, resources);
        }

        return requestService.executeApiResponse(HttpStatus.OK, "The article has been created!");
    }

    @PatchMapping("/edit")
    @Operation(summary = "Edit an article", description = "Provide an ID of article and changes to apply", responses = {
            @ApiResponse(responseCode = "200", description = "The request has been proceeded successfully"),
            @ApiResponse(responseCode = "10", description = "The authorized user is undefined"),
            @ApiResponse(responseCode = "11", description = "The authorized user has no authority to apply changes"),
            @ApiResponse(responseCode = "20", description = "Article user is undefined")
    })
    public ResponseEntity<?> editArticle(@Parameter(description = "An ID of article to apply changes", required = true) @RequestParam Long id, @Schema(implementation = Article.class, description = "The changed article data to put in") @RequestBody Map<String, Object> updates, @RequestParam(value = "newFiles", required = false) List<MultipartFile> newFiles, Model model) {
        Optional<User> wrappedUser = getAuthorizedUser();
        Optional<Article> wrappedArticle = articleService.findById(id);

//        if (!isUserAuthorized()) {
//            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, 10, "The authorized user is undefined!");
//        }

        if (wrappedArticle.isEmpty()) {
            return requestService.executeApiResponse(HttpStatus.BAD_REQUEST, "Article is undefined!");
        }

        Article newArticle = wrappedArticle.get();
        if (!newArticle.getAuthor().getId().equals(wrappedUser.get().getId())) {
            return requestService.executeApiResponse(HttpStatus.FORBIDDEN, "The authorized user has no authority to proceed the changes!");
        }

        updates.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(Article.class, key);
            if (field != null) {
                field.setAccessible(true);
                ReflectionUtils.setField(field, newArticle, value);
            }
        });

        articleService.save(newArticle);
        if (newFiles != null) {
            articleService.getResourceManager().updateResources(newArticle, newFiles);
        }

        return requestService.executeApiResponse(HttpStatus.OK, "The changes have been applied successfully!");
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete an article", description = "Provide an ID of article to delete", responses = {
            @ApiResponse(responseCode = "200", description = "The request has been proceeded successfully"),
            @ApiResponse(responseCode = "10", description = "The authorized user is undefined"),
            @ApiResponse(responseCode = "11", description = "The authorized user has no authority to apply changes"),
            @ApiResponse(responseCode = "20", description = "Article user is undefined")
    })
    public ResponseEntity<?> deleteArticle(@Parameter(description = "An ID of article to delete", required = true) @RequestParam Long id, Model model) {
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