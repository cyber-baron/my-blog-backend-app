package com.cyberbaron.blog.dto.comment;

public record UpdateCommentRequest(Long id, String text, Long postId) {

}