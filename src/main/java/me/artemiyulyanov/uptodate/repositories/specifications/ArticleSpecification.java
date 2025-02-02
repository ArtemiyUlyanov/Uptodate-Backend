package me.artemiyulyanov.uptodate.repositories.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import me.artemiyulyanov.uptodate.models.Article;
import me.artemiyulyanov.uptodate.models.ArticleTopic;
import me.artemiyulyanov.uptodate.models.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ArticleSpecification {
    public static Specification<Article> filterByTopics(List<String> topics) {
        return (root, q, criteriaBuilder) -> {
            if (topics == null || topics.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            Join<Article, ArticleTopic> topicsJoin = root.join("topics");

            List<Predicate> predicates = topics.stream()
                    .map(topic -> criteriaBuilder.or(
                            criteriaBuilder.equal(criteriaBuilder.function("JSON_UNQUOTE", String.class,
                                    criteriaBuilder.function("JSON_EXTRACT", String.class,
                                            topicsJoin.get("name"), criteriaBuilder.literal("$.english"))), topic),
                            criteriaBuilder.equal(criteriaBuilder.function("JSON_UNQUOTE", String.class,
                                    criteriaBuilder.function("JSON_EXTRACT", String.class,
                                            topicsJoin.get("name"), criteriaBuilder.literal("$.russian"))), topic)
                    ))
                    .toList();

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Article> filterByQuery(String query) {
        return (root, q, criteriaBuilder) -> {
            String pattern = "%" + query.toLowerCase() + "%";

            Join<Article, User> authorJoin = root.join("author");

            Predicate headingContaining = criteriaBuilder.like(criteriaBuilder.lower(root.get("heading")), pattern);
            Predicate descriptionContaining = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern);
            Predicate authorUsernameContaining = criteriaBuilder.like(criteriaBuilder.lower(authorJoin.get("username")), pattern);

            return criteriaBuilder.or(headingContaining, descriptionContaining, authorUsernameContaining);
        };
    }
}