package me.artemiyulyanov.uptodate.repositories;

import me.artemiyulyanov.uptodate.models.Article;
import me.artemiyulyanov.uptodate.models.User;
import me.artemiyulyanov.uptodate.repositories.specifications.ArticleSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {
    List<Article> findByAuthor(User author);
    Optional<Article> findByHeadingContainingAndAuthorId(String heading, Long authorId);

    @Query("SELECT e FROM Article e WHERE FUNCTION('DATE', e.createdAt) = :date AND e.heading LIKE %:heading%")
    List<Article> findByDateAndHeadingContaining(@Param("date") Date date, @Param("heading") String heading);
}