package com.cyberbaron.blog.repository;

import com.cyberbaron.blog.model.Post;
import org.springframework.data.repository.CrudRepository;

public interface PostRepository extends CrudRepository<Post, Long> {
}
