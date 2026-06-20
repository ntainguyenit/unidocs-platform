package com.unidocs.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtils {
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public static String toSlug(String input) {
        if (input == null) return "";
        
        // Convert Vietnamese characters 'Đ', 'đ'
        String text = input.replace("Đ", "d").replace("đ", "d");
        
        String nowhitespace = WHITESPACE.matcher(text.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        
        // Remove duplicate hyphens
        slug = slug.replaceAll("-+", "-");
        
        // Remove leading/trailing hyphens
        slug = slug.replaceAll("^-|-$", "");

        return slug.toLowerCase(Locale.ENGLISH);
    }
}
