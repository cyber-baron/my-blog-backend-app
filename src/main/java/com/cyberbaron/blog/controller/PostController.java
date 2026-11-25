package com.cyberbaron.blog.controller;

import com.cyberbaron.blog.dto.post.CreatePostRequest;
import com.cyberbaron.blog.dto.post.PostPageResponse;
import com.cyberbaron.blog.dto.post.PostResponse;
import com.cyberbaron.blog.dto.post.UpdatePostRequest;
import com.cyberbaron.blog.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService posts;

    public PostController(PostService posts) {
        this.posts = posts;
    }

    @GetMapping
    public PostPageResponse list(@RequestParam String search, @RequestParam int pageNumber,
                                 @RequestParam int pageSize) {
        return posts.list(search, pageNumber, pageSize);
    }

    @PostMapping
    public PostResponse create(@RequestBody CreatePostRequest request) {
        return posts.create(request);
    }

    @RequestMapping(path = "/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    public PostResponse get(@PathVariable long id) {
        return posts.get(id);
    }

    @PutMapping("/{id}")
    public PostResponse update(@PathVariable long id, @RequestBody UpdatePostRequest request) {
        return posts.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        posts.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/likes")
    public long like(@PathVariable long id) {
        return posts.incrementLikes(id);
    }
}