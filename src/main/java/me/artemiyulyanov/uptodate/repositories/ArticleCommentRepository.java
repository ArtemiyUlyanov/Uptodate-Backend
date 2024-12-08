package me.artemiyulyanov.uptodate.repositories;

import me.artemiyulyanov.uptodate.models.ArticleComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleCommentRepository extends JpaRepository<ArticleComment, Long> {
}