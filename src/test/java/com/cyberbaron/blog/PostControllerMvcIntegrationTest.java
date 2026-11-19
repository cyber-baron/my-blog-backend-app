package com.cyberbaron.blog;

import com.cyberbaron.blog.config.WebConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = WebConfig.class)
@Transactional
class PostControllerMvcIntegrationTest {
    @Autowired WebApplicationContext context;
    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private long createPost(String title, String text, String... tags) throws Exception {
        String payload = mapper.writeValueAsString(new com.cyberbaron.blog.dto.post.CreatePostRequest(title, text, java.util.List.of(tags)));
        String json = mvc.perform(post("/api/posts").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.likesCount").value(0))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(json).get("id").asLong();
    }

    @Test
    void implementsFeedGetPostPutDeleteAndLikes() throws Exception {
        long postId = createPost("Spring MVC", "Text", "java", "web");
        mvc.perform(get("/api/posts/{id}", postId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring MVC"));
        mvc.perform(post("/api/posts/{id}", postId)).andExpect(status().isOk()); // documented typo compatibility
        mvc.perform(post("/api/posts/{id}/likes", postId)).andExpect(status().isOk())
                .andExpect(content().string("1"));
        mvc.perform(put("/api/posts/{id}", postId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":" + postId + ",\"title\":\"Changed\",\"text\":\"Body\",\"tags\":[]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.likesCount").value(1));
        mvc.perform(get("/api/posts").param("search", "Changed").param("pageNumber", "1").param("pageSize", "5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.posts[0].title").value("Changed"));
        mvc.perform(delete("/api/posts/{id}", postId)).andExpect(status().isOk());
        mvc.perform(get("/api/posts/{id}", postId)).andExpect(status().isNotFound());
    }

    @Test
    void handlesSearchByMultipleTagsAndPagination() throws Exception {
        createPost("Java Spring", "A", "java", "spring");
        createPost("Java Spring MVC", "B", "java", "spring");
        createPost("Java JVM", "C", "java");
        mvc.perform(get("/api/posts").param("search", "Java #java #spring")
                        .param("pageNumber", "1").param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(1)))
                .andExpect(jsonPath("$.lastPage").value(2))
                .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    void supportsCommentsAndImagesAndCascadeDelete() throws Exception {
        long postId = createPost("Post", "Body");
        String created = mvc.perform(post("/api/posts/{id}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Hello\",\"postId\":" + postId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(postId))
                .andReturn().getResponse().getContentAsString();
        long commentId = mapper.readTree(created).get("id").asLong();
        mvc.perform(get("/api/posts/{id}/comments", postId)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("Hello"));
        mvc.perform(put("/api/posts/{postId}/comments/{id}", postId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + commentId + ",\"postId\":" + postId + ",\"text\":\"Edited\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.text").value("Edited"));
        mvc.perform(get("/api/posts/{postId}/comments/{id}", postId, commentId))
                .andExpect(status().isOk());
        byte[] png = new byte[]{(byte) 137, 80, 78, 71, 13, 10, 26, 10};
        var file = new MockMultipartFile("image", "sample.png", "image/png", png);
        mvc.perform(multipart("/api/posts/{id}/image", postId).file(file)
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isOk());
        mvc.perform(get("/api/posts/{id}/image", postId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(png));
        mvc.perform(delete("/api/posts/{id}", postId)).andExpect(status().isOk());
        mvc.perform(get("/api/posts/{postId}/comments/{id}", postId, commentId)).andExpect(status().isNotFound());
    }

    @Test
    void validatesBadRequestsAndMissingEntities() throws Exception {
        mvc.perform(get("/api/posts").param("search", "").param("pageNumber", "0").param("pageSize", "1"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/posts/999999")).andExpect(status().isNotFound());
        mvc.perform(post("/api/posts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"text\":\"Body\",\"tags\":[]}"))
                .andExpect(status().isBadRequest());
        long id = createPost("Valid", "Text");
        mvc.perform(get("/api/posts/{id}/image", id)).andExpect(status().isNotFound());
        mvc.perform(get("/api/posts").param("search", "").param("pageNumber", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void corsAllowsReactOrigin() throws Exception {
        mvc.perform(options("/api/posts")
                        .with(request -> {
                            request.setServerPort(8080);
                            return request; })
                        .header("Origin", "http://localhost")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin",
                        "http://localhost" ));
    }
}