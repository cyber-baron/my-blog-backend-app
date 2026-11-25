package com.cyberbaron.blog;

import com.cyberbaron.blog.config.DatabaseConfig;
import com.cyberbaron.blog.model.Comment;
import com.cyberbaron.blog.model.Post;
import com.cyberbaron.blog.repository.CommentRepository;
import com.cyberbaron.blog.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DatabaseConfig.class)
@Transactional
class PostRepositoryIntegrationTest {
    @Autowired PostRepository posts;
    @Autowired CommentRepository comments;
    @Autowired NamedParameterJdbcOperations jdbc;
    @Autowired PlatformTransactionManager transactionManager;

    @Test
    void storesAndReadsPostViaSpringDataJdbc() {
        Post saved = posts.save(new Post(null, "Spring Data JDBC", "Test body", 0));
        assertNotNull(saved.id());
        assertEquals("Spring Data JDBC", posts.findById(saved.id()).orElseThrow().title());
    }

    @Test
    void deletingPostCascadesToComments() {
        Post saved = posts.save(new Post(null, "Post", "Text", 0));
        Comment comment = comments.save(new Comment(null, saved.id(), "Hello"));
        assertEquals(1L, comments.countByPostId(saved.id()));
        posts.deleteById(saved.id());
        assertTrue(comments.findById(comment.id()).isEmpty());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void updatingContentPreservesLikeCommittedInSeparateTransaction() {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        Long postId = transaction.execute(status ->
                posts.save(new Post(null, "Original", "Original body", 2)).id());
        assertNotNull(postId);

        try {
            transaction.executeWithoutResult(editStatus -> {
                Post original = posts.findById(postId).orElseThrow();
                assertEquals(2L, original.likesCount());

                transaction.executeWithoutResult(likeStatus -> {
                    assertEquals(1, jdbc.update(
                            "UPDATE posts SET likes_count = likes_count + 1 WHERE id = :id",
                            java.util.Map.of("id", postId)));
                });
                assertEquals(1, posts.updateContent(original.id(), "Edited", "Edited body"));
            });

            Post updated = transaction.execute(status -> posts.findById(postId).orElseThrow());
            assertNotNull(updated);
            assertEquals("Edited", updated.title());
            assertEquals("Edited body", updated.text());
            assertEquals(3L, updated.likesCount());
        } finally {
            transaction.executeWithoutResult(status -> posts.deleteById(postId));
        }
    }
}