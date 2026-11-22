package com.cyberbaron.blog.service;

import java.util.List;

import com.cyberbaron.blog.dto.post.CreatePostRequest;
import com.cyberbaron.blog.dto.post.PostPageResponse;
import com.cyberbaron.blog.dto.post.PostResponse;
import com.cyberbaron.blog.dto.post.UpdatePostRequest;
import com.cyberbaron.blog.exception.NotFoundException;
import com.cyberbaron.blog.model.Post;
import com.cyberbaron.blog.repository.CommentRepository;
import com.cyberbaron.blog.repository.PostQueryDao;
import com.cyberbaron.blog.repository.PostRepository;
import com.cyberbaron.blog.repository.PostTagDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {
    private final PostRepository posts;
    private final CommentRepository comments;
    private final PostTagDao tags;
    private final PostQueryDao queries;

    public PostService(PostRepository posts, CommentRepository comments,
                       PostTagDao tags, PostQueryDao queries) {
        this.posts = posts;
        this.comments = comments;
        this.tags = tags;
        this.queries = queries;
    }

    @Transactional
    public PostResponse create(CreatePostRequest request) {
        if (request == null) throw new IllegalArgumentException("Request is required");
        validatePost(request.title(), request.text());
        List<String> normalizedTags = PostTagDao.normalize(request.tags());
        Post saved = posts.save(new Post(null, request.title().trim(), request.text(), 0));
        tags.replace(saved.id(), normalizedTags);
        return toResponse(saved, false);
    }

    @Transactional(readOnly = true)
    public PostResponse get(long id) {
        return toResponse(requirePost(id), false);
    }

    @Transactional
    public PostResponse update(long id, UpdatePostRequest request) {
        if (request == null) throw new IllegalArgumentException("Request is required");
        if (request.id() == null) throw new IllegalArgumentException("Post id is required");
        if (request.id() != id) {
            throw new IllegalArgumentException("Post id in body differs from path");
        }
        validatePost(request.title(), request.text());
        List<String> normalizedTags = PostTagDao.normalize(request.tags());
        Post old = requirePost(id);
        Post updated = posts.save(new Post(old.id(), request.title().trim(), request.text(), old.likesCount()));
        tags.replace(id, normalizedTags);
        return toResponse(updated, false);
    }

    @Transactional
    public void delete(long id) {
        requirePost(id);
        posts.deleteById(id);
    }

    @Transactional
    public long incrementLikes(long id) {
        return queries.incrementLikes(id);
    }

    @Transactional(readOnly = true)
    public PostPageResponse list(String search, int pageNumber, int pageSize) {
        if (pageNumber < 1 || pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("pageNumber must be >= 1; pageSize must be 1..100");
        }
        SearchCriteria criteria = SearchCriteria.parse(search);
        long total = queries.count(criteria.title(), criteria.tags());
        long lastPageLong = Math.max(1, (total + pageSize - 1) / pageSize);
        int lastPage = Math.toIntExact(Math.min(Integer.MAX_VALUE, lastPageLong));
        long offset = ((long) pageNumber - 1L) * pageSize;
        List<PostResponse> result = queries.findPage(criteria.title(), criteria.tags(), pageSize, offset)
                .stream().map(post -> toResponse(post, true)).toList();
        return new PostPageResponse(result, pageNumber > 1, pageNumber < lastPage, lastPage);
    }

    @Transactional(readOnly = true)
    public Post requirePost(long id) {
        if (id < 1) throw new IllegalArgumentException("Post id must be positive");
        return posts.findById(id).orElseThrow(() -> new NotFoundException("Post " + id + " not found"));
    }

    private PostResponse toResponse(Post post, boolean preview) {
        String text = preview ? truncatePreview(post.text()) : post.text();
        return new PostResponse(post.id(), post.title(), text, tags.findByPostId(post.id()),
                post.likesCount(), comments.countByPostId(post.id()));
    }

    public static String truncatePreview(String text) {
        if (text.codePointCount(0, text.length()) <= 128) return text;
        return text.substring(0, text.offsetByCodePoints(0, 128)) + "…";
    }

    private static void validatePost(String title, String text) {
        if (title == null || title.isBlank() || title.length() > 255) {
            throw new IllegalArgumentException("Post title must be 1..255 characters");
        }
        if (text == null || text.isBlank() || text.length() > 100_000) {
            throw new IllegalArgumentException("Post text must be 1..100000 characters");
        }
    }
}