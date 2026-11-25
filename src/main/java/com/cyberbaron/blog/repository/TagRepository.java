package com.cyberbaron.blog.repository;

import java.util.Optional;
import com.cyberbaron.blog.model.Tag;
import org.springframework.data.repository.CrudRepository;

public interface TagRepository extends CrudRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
}