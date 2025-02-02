package me.artemiyulyanov.uptodate.models.listeners;

import me.artemiyulyanov.uptodate.models.ArticleComment;
import me.artemiyulyanov.uptodate.services.ArticleCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArticleCommentListener {
    @Autowired
    public ArticleCommentListener(ArticleCommentService articleCommentService) {
        ArticleComment.setArticleCommentService(articleCommentService);
    }
}