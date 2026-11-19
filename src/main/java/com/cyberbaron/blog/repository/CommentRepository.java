package com.cyberbaron.blog.repository;

import java.util.List;
import com.cyberbaron.blog.model.Comment;
import org.springframework.data.repository.CrudRepository;

public interface CommentRepository extends CrudRepository<Comment, Long> {
    List<Comment> findAllByPostIdOrderByIdAsc(Long postId);
    long countByPostId(Long postId);
}