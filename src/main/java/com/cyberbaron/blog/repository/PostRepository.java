package com.cyberbaron.blog.repository;

import com.cyberbaron.blog.model.Post;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends CrudRepository<Post, Long> {
    @Modifying
    @Query("UPDATE posts SET title = :title, text = :text WHERE id = :id")
    int updateContent(@Param("id") long id, @Param("title") String title, @Param("text") String text);
}
