package com.cyberbaron.blog.repository;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.cyberbaron.blog.model.Post;
import com.cyberbaron.blog.exception.NotFoundException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

/** The feed uses SQL filtering/pagination; basic mutations use Spring Data CrudRepository */
@Repository
public class PostQueryDao {
    private final NamedParameterJdbcOperations jdbc;

    public PostQueryDao(NamedParameterJdbcOperations jdbc) {
        this.jdbc = jdbc;
    }

    private String whereClause(List<String> tags, MapSqlParameterSource params) {
        StringBuilder where = new StringBuilder(" WHERE LOCATE(:title, LOWER(p.title)) > 0");
        for (int i = 0; i < tags.size(); i++) {
            String key = "tag" + i;
            params.addValue(key, tags.get(i));
            where.append(" AND EXISTS (SELECT 1 FROM post_tags pt")
                 .append(" JOIN tags t ON t.id = pt.tag_id")
                 .append(" WHERE pt.post_id = p.id AND t.name = :").append(key).append(')');
        }
        return where.toString();
    }

    public long incrementLikes(long postId) {
        if (postId < 1) throw new IllegalArgumentException("Post id must be positive");
        MapSqlParameterSource params = new MapSqlParameterSource("postId", postId);
        int updated = jdbc.update("UPDATE posts SET likes_count = likes_count + 1 WHERE id = :postId", params);
        if (updated == 0) throw new NotFoundException("Post " + postId + " not found");
        Long count = jdbc.queryForObject("SELECT likes_count FROM posts WHERE id = :postId", params, Long.class);
        return java.util.Objects.requireNonNull(count);
    }

    public long count(String title, List<String> tags) {
        MapSqlParameterSource params = new MapSqlParameterSource("title", title.toLowerCase(java.util.Locale.ROOT));
        String where = whereClause(tags, params);
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM posts p" + where, params, Long.class);
        return count == null ? 0 : count;
    }

    public Map<Long, Long> countCommentsByPostIds(List<Long> postIds) {
        if (postIds.isEmpty()) return Map.of();
        Map<Long, Long> result = new HashMap<>();
        jdbc.query("""
                SELECT post_id, COUNT(*) AS comments_count FROM comments
                WHERE post_id IN (:postIds) GROUP BY post_id
                """, new MapSqlParameterSource("postIds", postIds), rs -> {
            result.put(rs.getLong("post_id"), rs.getLong("comments_count"));
        });
        return result;
    }

    public List<Post> findPage(String title, List<String> tags, int pageSize, long offset) {
        MapSqlParameterSource params = new MapSqlParameterSource("title", title.toLowerCase(java.util.Locale.ROOT))
                .addValue("limit", pageSize).addValue("offset", offset);
        String where = whereClause(tags, params);
        return jdbc.query("""
                        SELECT p.id, p.title, p.text, p.likes_count FROM posts p
                        """ + where + " ORDER BY p.id DESC LIMIT :limit OFFSET :offset",
                params, (rs, rowNum) -> new Post(rs.getLong("id"), rs.getString("title"),
                        rs.getString("text"), rs.getLong("likes_count")));
    }
}