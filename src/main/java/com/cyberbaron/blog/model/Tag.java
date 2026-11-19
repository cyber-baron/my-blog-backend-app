package com.cyberbaron.blog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("TAGS")
public record Tag(@Id Long id, String name) {
}