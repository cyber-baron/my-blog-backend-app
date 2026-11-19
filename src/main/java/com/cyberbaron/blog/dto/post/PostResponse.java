package com.cyberbaron.blog.dto.post;

import java.util.List;

public record PostResponse(
        Long id,
        String title,
        String text,
        List<String> tags,
        long likesCount,
        long commentsCount
) { }