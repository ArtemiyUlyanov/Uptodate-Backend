package me.artemiyulyanov.uptodate.services;

import me.artemiyulyanov.uptodate.models.*;
import me.artemiyulyanov.uptodate.repositories.CommentLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommentLikeService {
    @Autowired
    private CommentLikeRepository commentLikeRepository;

    public void like(Comment comment, User user) {
        if (!commentLikeRepository.existsByCommentAndUser(comment, user)) {
            CommentLike commentLike = CommentLike
                    .builder()
                    .comment(comment)
                    .user(user)
                    .likedAt(LocalDateTime.now())
                    .build();

            commentLikeRepository.save(commentLike);
        } else {
            Optional<CommentLike> wrappedCommentLike = commentLikeRepository.findByCommentAndUser(comment, user);
            commentLikeRepository.delete(wrappedCommentLike.get());
        }
    }

    public List<CommentLike> findLastLikesOfAuthor(User user, LocalDateTime after) {
        return commentLikeRepository.findLastLikesOfAuthor(user, after);
    }
}