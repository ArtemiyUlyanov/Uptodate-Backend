package me.artemiyulyanov.uptodate.repositories;

import me.artemiyulyanov.uptodate.models.Article;
import me.artemiyulyanov.uptodate.models.ArticleComment;
import me.artemiyulyanov.uptodate.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleCommentRepository extends JpaRepository<ArticleComment, Long> {
    List<ArticleComment> findByArticle(Article article);
    List<ArticleComment> findByAuthor(User author);
}