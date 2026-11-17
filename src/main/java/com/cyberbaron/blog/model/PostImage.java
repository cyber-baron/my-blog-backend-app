package com.cyberbaron.blog.model;

/** Image content stored separately from the post's text fields */
public record PostImage(Long postId, byte[] imageData, String contentType) {

    public PostImage {
        imageData = imageData == null ? null : imageData.clone();
    }

    @Override
    public byte[] imageData() {
        return imageData == null ? null : imageData.clone();
    }
}