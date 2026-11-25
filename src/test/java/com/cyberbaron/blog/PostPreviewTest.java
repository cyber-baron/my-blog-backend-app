package com.cyberbaron.blog;

import com.cyberbaron.blog.service.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostPreviewTest {
    @ParameterizedTest
    @ValueSource(strings = {"\n\n", "\r\n\r\n", "\r\r", "\n \t\n"})
    void returnsOnlyFirstParagraph(String separator) {
        assertEquals("First paragraph", PostService.firstParagraph("First paragraph" + separator + "Second paragraph"));
    }

    @Test
    void preservesSingleLineBreakWithinParagraph() {
        assertEquals("First line\nSecond line",
                PostService.firstParagraph("First line\nSecond line\n\nNext paragraph"));
    }

    @Test
    void skipsLeadingBlankLines() {
        assertEquals("First paragraph", PostService.firstParagraph("\n \t\nFirst paragraph\n\nSecond paragraph"));
    }

    @Test
    void preservesLongParagraphWithoutAddingEllipsis() {
        String paragraph = "Long paragraph 🌍 ".repeat(50);
        assertEquals(paragraph, PostService.firstParagraph(paragraph + "\n\nSecond paragraph"));
    }

    @Test
    void returnsSingleParagraphUnchanged() {
        assertEquals("Hello", PostService.firstParagraph("Hello"));
    }
}
