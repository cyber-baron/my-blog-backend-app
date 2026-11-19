package com.cyberbaron.blog.repository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.cyberbaron.blog.model.Tag;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

/** Join-table DAO: Spring Data JDBC intentionally does not model JPA-style many-to-many relations */
@Repository
public class PostTagDao {
    private final NamedParameterJdbcOperations jdbc;
    private final TagRepository tags;

    public PostTagDao(NamedParameterJdbcOperations jdbc, TagRepository tags) {
        this.jdbc = jdbc;
        this.tags = tags;
    }

    public List<String> findByPostId(long postId) {
        return jdbc.query("""
                SELECT t.name FROM tags t
                JOIN post_tags pt ON pt.tag_id = t.id
                WHERE pt.post_id = :postId ORDER BY t.name
                """, new MapSqlParameterSource("postId", postId),
                (rs, rowNum) -> rs.getString(1));
    }

    public void replace(long postId, List<String> rawTags) {
        List<String> names = normalize(rawTags);
        jdbc.update("DELETE FROM post_tags WHERE post_id = :postId",
                new MapSqlParameterSource("postId", postId));
        for (String name : names) {
            Tag tag = tags.findByName(name).orElseGet(() -> tags.save(new Tag(null, name)));
            jdbc.update("INSERT INTO post_tags(post_id, tag_id) VALUES (:postId, :tagId)",
                    new MapSqlParameterSource("postId", postId).addValue("tagId", tag.id()));
        }
    }

    public static List<String> normalize(List<String> rawTags) {
        if (rawTags == null) throw new IllegalArgumentException("tags must be supplied");
        Set<String> result = new LinkedHashSet<>();
        for (String raw : rawTags) {
            if (raw == null) throw new IllegalArgumentException("Tag cannot be null");
            String name = raw.trim().toLowerCase(Locale.ROOT);
            if (name.startsWith("#")) name = name.substring(1);
            if (name.isBlank() || name.length() > 100 || name.chars().anyMatch(Character::isWhitespace)) {
                throw new IllegalArgumentException("Tags must be nonempty words of at most 100 characters");
            }
            result.add(name);
        }
        return new ArrayList<>(result);
    }
}