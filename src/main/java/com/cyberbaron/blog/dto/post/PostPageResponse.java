package com.cyberbaron.blog.dto.post;

import java.util.List;

public record PostPageResponse(
        List<PostResponse> posts,
        boolean hasPrev,
        boolean hasNext,
        int lastPage
) { }