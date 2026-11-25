package com.cyberbaron.blog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** Root entity; tags, comments and image are persisted separately */
@Table("POSTS")
public record Post(@Id Long id, String title, String text,
                   @Column("LIKES_COUNT") long likesCount) {
}