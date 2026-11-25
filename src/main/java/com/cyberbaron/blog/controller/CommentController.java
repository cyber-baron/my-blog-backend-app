package com.cyberbaron.blog.controller;

import java.util.List;
import com.cyberbaron.blog.dto.comment.CommentResponse;
import com.cyberbaron.blog.dto.comment.CreateCommentRequest;
import com.cyberbaron.blog.dto.comment.UpdateCommentRequest;
import com.cyberbaron.blog.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {
    private final CommentService comments;

    public CommentController(CommentService comments) {
        this.comments = comments;
    }

    @GetMapping
    public List<CommentResponse> list(@PathVariable long postId) {
        return comments.list(postId);
    }

    @GetMapping("/{commentId}")
    public CommentResponse get(@PathVariable long postId, @PathVariable long commentId) {
        return comments.get(postId, commentId);
    }

    @PostMapping
    public CommentResponse create(@PathVariable long postId, @RequestBody CreateCommentRequest request) {
        return comments.create(postId, request);
    }

    @PutMapping("/{commentId}")
    public CommentResponse update(@PathVariable long postId, @PathVariable long commentId,
                                  @RequestBody UpdateCommentRequest request) {
        return comments.update(postId, commentId, request);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable long postId, @PathVariable long commentId) {
        comments.delete(postId, commentId);
        return ResponseEntity.ok().build();
    }

}