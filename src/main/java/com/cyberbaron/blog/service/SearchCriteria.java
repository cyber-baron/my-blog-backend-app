package com.cyberbaron.blog.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public record SearchCriteria(String title, List<String> tags) {
    public static SearchCriteria parse(String raw) {
        if (raw == null) throw new IllegalArgumentException("search is required");
        List<String> titleWords = new ArrayList<>();
        Set<String> tags = new LinkedHashSet<>();
        for (String term : raw.trim().split("\\s+")) {
            if (term.isEmpty()) continue;
            if (term.startsWith("#") && term.length() > 1) {
                tags.add(term.substring(1).toLowerCase(Locale.ROOT));
            } else if (!term.equals("#")) {
                titleWords.add(term);
            }
        }
        return new SearchCriteria(String.join(" ", titleWords), List.copyOf(tags));
    }
}