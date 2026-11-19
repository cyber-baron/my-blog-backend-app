package com.cyberbaron.blog.dto.comment;

public record CreateCommentRequest(String text, Long postId) {

}