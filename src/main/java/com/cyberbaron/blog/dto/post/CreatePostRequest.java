package com.cyberbaron.blog.dto.post;

import java.util.List;

public record CreatePostRequest(String title, String text, List<String> tags) {

}