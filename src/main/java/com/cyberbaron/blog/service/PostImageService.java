package com.cyberbaron.blog.service;

import java.io.IOException;
import java.util.Set;
import com.cyberbaron.blog.exception.NotFoundException;
import com.cyberbaron.blog.model.PostImage;
import com.cyberbaron.blog.repository.PostImageDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PostImageService {
    public static final long MAX_IMAGE_BYTES = 10 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/png", "image/jpeg", "image/gif", "image/webp");

    private final PostImageDao images;
    private final PostService posts;

    public PostImageService(PostImageDao images, PostService posts) {
        this.images = images;
        this.posts = posts;
    }

    @Transactional
    public void save(long postId, MultipartFile image) throws IOException {
        posts.requirePost(postId);
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image is empty");
        }
        if (image.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("Image exceeds 10 MB");
        }
        String contentType = image.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported image type");
        }
        images.save(postId, image.getBytes(), contentType);
    }

    @Transactional(readOnly = true)
    public PostImage get(long postId) {
        posts.requirePost(postId);
        return images.findByPostId(postId)
                .orElseThrow(() -> new NotFoundException("Image for post " + postId + " not found"));
    }
}