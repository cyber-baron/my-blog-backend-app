package com.cyberbaron.blog;

import com.cyberbaron.blog.config.WebConfig;
import com.cyberbaron.blog.dto.comment.CreateCommentRequest;
import com.cyberbaron.blog.dto.post.CreatePostRequest;
import com.cyberbaron.blog.dto.post.PostPageResponse;
import com.cyberbaron.blog.dto.post.UpdatePostRequest;
import com.cyberbaron.blog.service.CommentService;
import com.cyberbaron.blog.service.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = WebConfig.class)
@Transactional
class PostServiceIntegrationTest {
    @Autowired PostService posts;
    @Autowired CommentService comments;

    @Test
    void createsUpdatesAndDeletesWithBusinessRules() {
        var created = posts.create(new CreatePostRequest("  Post  ", "Body", List.of("#Java", "spring", "java")));
        assertEquals("Post", created.title());
        assertEquals(List.of("java", "spring"), created.tags());
        assertEquals(0L, created.likesCount());
        assertEquals(1L, posts.incrementLikes(created.id()));
        assertEquals(2L, posts.incrementLikes(created.id()));
        comments.create(created.id(), new CreateCommentRequest("A comment", created.id()));
        assertEquals(1L, posts.get(created.id()).commentsCount());
        var updated = posts.update(created.id(), new UpdatePostRequest(created.id(), "Updated", "New body", List.of("h2")));
        assertEquals(2L, updated.likesCount());
        assertEquals(List.of("h2"), updated.tags());
        assertEquals("Updated", updated.title());
        posts.delete(created.id());
        assertThrows(RuntimeException.class, () -> posts.get(created.id()));
    }

    @Test
    void searchAndPaginationRespectAllTagsAndTitle() {
        posts.create(new CreatePostRequest("Spring MVC Guide", "X".repeat(135), List.of("java", "mvc")));
        posts.create(new CreatePostRequest("Spring Data", "Body", List.of("java", "database")));
        posts.create(new CreatePostRequest("Spring MVC Advanced", "Body", List.of("java", "mvc")));
        PostPageResponse page1 = posts.list("spring #java #mvc", 1, 1);
        assertEquals(2, page1.lastPage());
        assertEquals(1, page1.posts().size());
        assertFalse(page1.hasPrev());
        assertTrue(page1.hasNext());
        PostPageResponse page2 = posts.list("spring #java #mvc", 2, 1);
        assertTrue(page2.hasPrev());
        assertFalse(page2.hasNext());
        assertTrue(page2.posts().get(0).text().endsWith("…"));
        assertEquals(129, page2.posts().get(0).text().length());
    }
}