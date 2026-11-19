package com.cyberbaron.blog.repository;

import java.util.Optional;
import com.cyberbaron.blog.model.PostImage;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

@Repository
public class PostImageDao {
    private final NamedParameterJdbcOperations jdbc;

    public PostImageDao(NamedParameterJdbcOperations jdbc) {
        this.jdbc = jdbc;
    }

    public void save(long postId, byte[] bytes, String contentType) {
        jdbc.update("""
                MERGE INTO post_images (post_id, image_data, content_type)
                KEY (post_id) VALUES (:postId, :bytes, :contentType)
                """, new MapSqlParameterSource("postId", postId)
                    .addValue("bytes", bytes).addValue("contentType", contentType));
    }

    public Optional<PostImage> findByPostId(long postId) {
        var found = jdbc.query("""
                SELECT post_id, image_data, content_type FROM post_images WHERE post_id = :postId
                """, new MapSqlParameterSource("postId", postId),
                (rs, rowNum) -> new PostImage(rs.getLong("post_id"),
                        rs.getBytes("image_data"), rs.getString("content_type")));
        return found.stream().findFirst();
    }
}