package me.artemiyulyanov.uptodate.services;

import jakarta.persistence.Transient;
import me.artemiyulyanov.uptodate.models.Article;
import me.artemiyulyanov.uptodate.models.ArticleLike;
import me.artemiyulyanov.uptodate.models.ArticleView;
import me.artemiyulyanov.uptodate.models.User;
import me.artemiyulyanov.uptodate.repositories.ArticleLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ArticleLikeService {
    @Autowired
    private ArticleLikeRepository articleLikeRepository;

    public void like(Article article, User user) {
        if (!articleLikeRepository.existsByArticleAndUser(article, user)) {
            ArticleLike articleLike = ArticleLike
                    .builder()
                    .article(article)
                    .user(user)
                    .likedAt(LocalDateTime.now())
                    .build();

            articleLikeRepository.save(articleLike);
        } else {
            Optional<ArticleLike> wrappedArticleLike = articleLikeRepository.findByArticleAndUser(article, user);
            articleLikeRepository.delete(wrappedArticleLike.get());
        }
    }

    public List<ArticleLike> findLastLikesOfAuthor(User user, LocalDateTime after) {
        return articleLikeRepository.findLastLikesOfAuthor(user, after);
    }
}