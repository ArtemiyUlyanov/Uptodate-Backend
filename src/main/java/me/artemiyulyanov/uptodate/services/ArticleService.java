package me.artemiyulyanov.uptodate.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import me.artemiyulyanov.uptodate.minio.MinioService;
import me.artemiyulyanov.uptodate.minio.resources.ArticleResourceManager;
import me.artemiyulyanov.uptodate.minio.resources.UserResourceManager;
import me.artemiyulyanov.uptodate.models.Article;
import me.artemiyulyanov.uptodate.models.ArticleTopic;
import me.artemiyulyanov.uptodate.models.User;
import me.artemiyulyanov.uptodate.models.text.ArticleTextFragment;
import me.artemiyulyanov.uptodate.repositories.ArticleRepository;
import me.artemiyulyanov.uptodate.web.PageableObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ArticleService implements ResourceService<ArticleResourceManager> {
    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    @Lazy
    private ArticleTopicService articleTopicService;

    @Autowired
    private MinioService minioService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    @Lazy
    public void init() {
        if (articleRepository.count() > 0) return;

        ArticleTopic topic1 = articleTopicService.findByName("Cultural Travel").get();
        ArticleTopic topic2 = articleTopicService.findByName("Luxury Travel").get();

        ArticleTopic topic3 = articleTopicService.findByName("Blockchain").get();
        ArticleTopic topic4 = articleTopicService.findByName("Cloud Computing").get();

        ArticleTopic topic5 = articleTopicService.findByName("Fashion & Style").get();

        User author = userService.findByUsername("artemiyulyanov2008").get();
        Article article1 = Article.builder()
                .author(author)
                .heading("No longer unavailable — widely-distributed famous fashion brands appear in Shanghai’s streets")
                .description("The recent surge of globally renowned fashion brands establishing a strong presence in Shanghai’s retail scene.")
                .content(List.of(
                                ArticleTextFragment
                                        .builder()
                                        .text("The content of the article #1")
                                        .type(ArticleTextFragment.ArticleTextFragmentType.DEFAULT)
                                        .build()
                        )
                )
                .topics(Set.of(topic1, topic2))
                .createdAt(LocalDateTime.now())
                .build();

        Article article2 = Article.builder()
                .author(author)
                .heading("Top 10 places to visit in Amsterdam — the last one is the most wondering")
                .description("The top 10 must-visit locations in Amsterdam, showcasing the city’s iconic canals, historical landmarks, and vibrant cultural spots.")
                .content(List.of(
                                ArticleTextFragment
                                        .builder()
                                        .text("The content of the article #2")
                                        .type(ArticleTextFragment.ArticleTextFragmentType.DEFAULT)
                                        .build()
                        )
                )
                .topics(Set.of(topic3, topic4))
                .createdAt(LocalDateTime.now())
                .build();

        Article article3 = Article.builder()
                .author(author)
                .heading("The future is up to you — the programming languages to be demanded in 2024")
                .description("The programming languages predicted to be in high demand in 2024, as technology continues to evolve at a rapid pace.")
                .content(List.of(
                                ArticleTextFragment
                                        .builder()
                                        .text("The content of the article #2")
                                        .type(ArticleTextFragment.ArticleTextFragmentType.DEFAULT)
                                        .build()
                        )
                )
                .topics(Set.of(topic5))
                .createdAt(LocalDateTime.now())
                .build();

        articleRepository.save(article1);
        articleRepository.save(article2);
        articleRepository.save(article3);
    }

    public long count() {
        return articleRepository.count();
    }

    public List<Article> findAllArticles(Sort sort) {
        return articleRepository.findAll(sort);
    }

    public Page<Article> findAllArticles(PageableObject<Article> pageableObject) {
        return articleRepository.findAll(pageableObject.getCommonSpecification(), pageableObject.getPageable());
    }

    public Page<Article> findAllArticles(PageRequest pageRequest) {
        return articleRepository.findAll(pageRequest);
    }

    public Optional<Article> findById(Long id) {
        return articleRepository.findById(id);
    }

    public List<Article> findByAuthor(User author) {
        return articleRepository.findByAuthor(author);
    }

    public void deleteById(Long id) {
        articleRepository.deleteById(id);
    }

    public void delete(Article article) {
        getResourceManager().deleteResources(article);
        articleRepository.delete(article);
    }

    public void save(Article article) {
        articleRepository.save(article);
    }

    @Override
    public ArticleResourceManager getResourceManager() {
        return ArticleResourceManager
                .builder()
                .minioService(minioService)
                .build();
    }

//    @Override
//    public void uploadResources(Article article, List<MultipartFile> resources) {
//        if (resources != null) {
//            minioService.saveArticleResources(article, resources);
//        }
//    }
//
//    @Override
//    public void updateResources(Article article) {
//        List<String> images = article.getContent()
//                .stream()
//                .filter(fragment -> fragment.getType() == ArticleTextFragment.ArticleTextFragmentType.IMAGE)
//                .map(ArticleTextFragment::getText)
//                .toList();
//
//        List<String> resources = minioService.getArticleResources(article);
//        resources.stream()
//                .filter(resource -> !images.contains(resource))
//                .forEach(resource -> minioService.removeFile(resource));
//    }
//
//    @Override
//    public void deleteResources(Article article) {
//        if (minioService.fileExists(getResourceFolder(article))) minioService.deleteFolder(getResourceFolder(article));
//    }

//    @Deprecated
//    public void update(Article article, List<MultipartFile> resources) {
//        if (resources != null) {
//            minioService.saveArticleResources(article, resources);
//        }
//
//        articleRepository.save(article);
//    }

    //    public List<ArticleTextFragment> getArticleTextFragments(Article article) {
//        try {
//            return objectMapper.readValue(article.getContent(), objectMapper.getTypeFactory().constructCollectionType(List.class, ArticleTextFragment.class));
//        } catch (IOException e) {
//            return new ArrayList<>();
//        }
//    }
}