package com.cyberbaron.blog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("COMMENTS")
public record Comment(@Id Long id, @Column("POST_ID") Long postId, String text) {
}