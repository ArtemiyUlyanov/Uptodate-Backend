package me.artemiyulyanov.uptodate;


import me.artemiyulyanov.uptodate.models.Comment;
import me.artemiyulyanov.uptodate.repositories.CommentRepository;
import me.artemiyulyanov.uptodate.services.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetComment() {
        Comment comment = Comment.builder()
                .id(52L)
                .content("Comment 1")
                .build();

        when(commentRepository.findById(52L)).thenReturn(Optional.of(comment));

        Comment retrievedComment1 = commentService.findById(52L).orElse(null);
        Comment retrievedComment2 = commentService.findById(51L).orElse(null);

        assertNotNull(retrievedComment1);
        assertEquals(retrievedComment1.getId(), 52);
        assertNotEquals(retrievedComment2.getId(), 52);
    }
}