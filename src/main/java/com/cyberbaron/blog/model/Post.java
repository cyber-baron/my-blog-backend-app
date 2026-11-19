package com.cyberbaron.blog.model;

/** Persisted post data. Tags, image and comment count are handled separately */
public record Post(Long id, String title, String text, long likesCount) {

}