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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
//        Article article1 = articleRepository.findById(1L).get();
//        System.out.println("OOPS I DID IT AGAIN I HAVE PLAYED WITH YOUR HEART GOT LOST IN THIS GAME");
//        minioService.getArticleResources(article1).forEach(System.out::println);

        if (articleRepository.count() > 0) return;

        ArticleTopic topic1 = articleTopicService.findByName("Cultural Travel").get();
        ArticleTopic topic2 = articleTopicService.findByName("Luxury Travel").get();

        User author = userService.findByUsername("Artemiy").get();
        for (int i = 1; i <= 50; i++) {
            Article article = Article.builder()
                    .author(author)
                    .heading("The heading of the article #" + i)
                    .content(List.of(
                            ArticleTextFragment
                                .builder()
                                .text("The content of the article #" + i)
                                .type(ArticleTextFragment.ArticleTextFragmentType.DEFAULT)
                                .build()
                            )
                    )
//                    .content("Test")
                    .topics(Set.of(topic1, topic2))
                    .createdAt(LocalDateTime.now())
                    .build();
            articleRepository.save(article);
        }
    }

    public List<Article> findAllArticles(Sort sort) {
        return articleRepository.findAll(sort);
    }

    public Page<Article> findAllArticles(Pageable pageable) {
        return articleRepository.findAll(pageable);
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