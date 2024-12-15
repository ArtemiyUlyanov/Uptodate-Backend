package me.artemiyulyanov.uptodate.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.artemiyulyanov.uptodate.minio.MinioService;
import me.artemiyulyanov.uptodate.models.Article;
import me.artemiyulyanov.uptodate.models.ArticleComment;
import me.artemiyulyanov.uptodate.models.User;
import me.artemiyulyanov.uptodate.repositories.ArticleCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleCommentService {
    @Autowired
    private ArticleCommentRepository articleCommentRepository;

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    private ArticleTopicService articleTopicService;

    @Autowired
    private MinioService minioService;

    @Autowired
    private ObjectMapper objectMapper;

    public Optional<ArticleComment> findById(Long id) {
        return articleCommentRepository.findById(id);
    }

    public List<ArticleComment> findByArticle(Article article) {
        return articleCommentRepository.findByArticle(article);
    }

    public List<ArticleComment> findByAuthor(User author) {
        return articleCommentRepository.findByAuthor(author);
    }

    public void delete(ArticleComment comment) {
        minioService.deleteArticleCommentResources(comment);
        articleCommentRepository.delete(comment);
    }

    public void save(ArticleComment comment) {
        articleCommentRepository.save(comment);
    }

    public void loadResources(ArticleComment comment, List<MultipartFile> resources) {
        if (resources != null) {
            minioService.deleteArticleCommentResources(comment);
            minioService.saveArticleCommentResources(comment, resources);
        }
    }

    @Deprecated
    public void update(ArticleComment comment, List<MultipartFile> resources) {
        if (resources != null) {
            minioService.saveArticleCommentResources(comment, resources);
        }

        articleCommentRepository.save(comment);
    }
}