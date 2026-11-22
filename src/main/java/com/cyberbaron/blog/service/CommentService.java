package com.cyberbaron.blog.service;

import java.util.List;
import com.cyberbaron.blog.dto.comment.CommentResponse;
import com.cyberbaron.blog.dto.comment.CreateCommentRequest;
import com.cyberbaron.blog.dto.comment.UpdateCommentRequest;
import com.cyberbaron.blog.exception.NotFoundException;
import com.cyberbaron.blog.model.Comment;
import com.cyberbaron.blog.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {
    private final CommentRepository comments;
    private final PostService posts;

    public CommentService(CommentRepository comments, PostService posts) {
        this.comments = comments;
        this.posts = posts;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> list(long postId) {
        posts.requirePost(postId);
        return comments.findAllByPostIdOrderByIdAsc(postId).stream().map(CommentService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CommentResponse get(long postId, long commentId) {
        posts.requirePost(postId);
        return toResponse(requireComment(postId, commentId));
    }

    @Transactional
    public CommentResponse create(long postId, CreateCommentRequest request) {
        if (request == null) throw new IllegalArgumentException("Request is required");
        posts.requirePost(postId);
        checkBodyPostId(postId, request.postId());
        validateText(request.text());
        return toResponse(comments.save(new Comment(null, postId, request.text())));
    }

    @Transactional
    public CommentResponse update(long postId, long commentId, UpdateCommentRequest request) {
        if (request == null) throw new IllegalArgumentException("Request is required");
        posts.requirePost(postId);
        checkBodyPostId(postId, request.postId());
        if (request.id() == null) throw new IllegalArgumentException("Comment id is required");
        if (request.id() != commentId) {
            throw new IllegalArgumentException("Comment id in body differs from path");
        }
        validateText(request.text());
        Comment old = requireComment(postId, commentId);
        return toResponse(comments.save(new Comment(old.id(), old.postId(), request.text())));
    }

    @Transactional
    public void delete(long postId, long commentId) {
        posts.requirePost(postId);
        Comment old = requireComment(postId, commentId);
        comments.deleteById(old.id());
    }

    private Comment requireComment(long postId, long id) {
        if (id < 1) throw new IllegalArgumentException("Comment id must be positive");
        return comments.findById(id)
                .filter(c -> c.postId().equals(postId))
                .orElseThrow(() -> new NotFoundException("Comment " + id + " not found for post " + postId));
    }

    private static void checkBodyPostId(long pathPostId, Long bodyPostId) {
        if (bodyPostId == null || bodyPostId != pathPostId) {
            throw new IllegalArgumentException("postId in body must match URL");
        }
    }

    private static void validateText(String text) {
        if (text == null || text.isBlank() || text.length() > 10_000) {
            throw new IllegalArgumentException("Comment text must be 1..10000 characters");
        }
    }

    private static CommentResponse toResponse(Comment c) {
        return new CommentResponse(c.id(), c.text(), c.postId());
    }
}