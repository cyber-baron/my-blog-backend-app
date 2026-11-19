package com.cyberbaron.blog;

import com.cyberbaron.blog.service.PostService;
import com.cyberbaron.blog.service.SearchCriteria;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchCriteriaTest {
    @Test
    void splitsTitleWordsAndAndedTags() {
        SearchCriteria search = SearchCriteria.parse("  Spring   #Java  Framework #h2 #java ");
        assertEquals("Spring Framework", search.title());
        assertEquals(List.of("java", "h2"), search.tags());
    }

    @Test
    void supportsEmptySearch() {
        SearchCriteria search = SearchCriteria.parse("  ");
        assertEquals("", search.title());
        assertTrue(search.tags().isEmpty());
    }

    @Test
    void previewsHaveExactly128CodepointsThenEllipsis() {
        String text = "_".repeat(129);
        String preview = PostService.truncatePreview(text);
        assertEquals(129, preview.codePointCount(0, preview.length()));
        assertTrue(preview.endsWith("…"));
        assertEquals(text.substring(0, text.offsetByCodePoints(0, 128)) + "…", preview);
    }

    @Test
    void doesNotTruncateShortText() {
        assertEquals("Hello", PostService.truncatePreview("Hello"));
    }
}