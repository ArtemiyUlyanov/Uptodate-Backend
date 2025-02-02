package me.artemiyulyanov.uptodate.repositories;

import me.artemiyulyanov.uptodate.models.ArticleTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleTopicRepository extends JpaRepository<ArticleTopic, Long> {
    @Query(value = "SELECT * FROM topics WHERE JSON_UNQUOTE(JSON_EXTRACT(parent, '$.english')) = :parent OR JSON_UNQUOTE(JSON_EXTRACT(parent, '$.russian')) = :parent", nativeQuery = true)
    List<ArticleTopic> findByParentInEnglishOrRussian(@Param("parent") String parent);

    @Query(value = "SELECT * FROM topics WHERE JSON_UNQUOTE(JSON_EXTRACT(name, '$.english')) = :name OR JSON_UNQUOTE(JSON_EXTRACT(name, '$.russian')) = :name", nativeQuery = true)
    Optional<ArticleTopic> findByNameInEnglishOrRussian(@Param("name") String name);
}