package com.cyberbaron.blog.controller;

import java.io.IOException;
import com.cyberbaron.blog.model.PostImage;
import com.cyberbaron.blog.service.PostImageService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts/{postId}/image")
public class PostImageController {
    private final PostImageService images;

    public PostImageController(PostImageService images) {
        this.images = images;
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> upload(@PathVariable long postId,
                                       @RequestParam("image") MultipartFile image) throws IOException {
        images.save(postId, image);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<byte[]> get(@PathVariable long postId) {
        PostImage image = images.get(postId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .cacheControl(CacheControl.noStore())
                .body(image.imageData());
    }
}