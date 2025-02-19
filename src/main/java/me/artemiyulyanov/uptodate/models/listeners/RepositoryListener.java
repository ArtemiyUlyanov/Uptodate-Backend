package me.artemiyulyanov.uptodate.models.listeners;

import me.artemiyulyanov.uptodate.models.ArticleComment;
import me.artemiyulyanov.uptodate.models.UserStatistics;
import me.artemiyulyanov.uptodate.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RepositoryListener {
    @Autowired
    public RepositoryListener(ArticleCommentService articleCommentService, ArticleService articleService, ArticleViewService articleViewService, ArticleLikeService articleLikeService) {
        ArticleComment.setArticleCommentService(articleCommentService);
        UserStatistics.setArticleLikeService(articleLikeService);
        UserStatistics.setArticleViewService(articleViewService);
    }
}