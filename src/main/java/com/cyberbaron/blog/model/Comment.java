package com.cyberbaron.blog.model;

/** Comment belongs to a post identified by postId */
public record Comment(Long id, Long postId, String text) {

}