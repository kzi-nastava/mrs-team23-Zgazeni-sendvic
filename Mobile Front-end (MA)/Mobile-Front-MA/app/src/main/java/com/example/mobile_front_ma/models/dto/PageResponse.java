package com.example.mobile_front_ma.models.dto;

import java.util.List;

/**
 * Mirrors a Spring Data {@code Page<T>} JSON payload. The backend HOR / account
 * endpoints return paginated results, so we only need the slice of fields the UI uses.
 */
public class PageResponse<T> {

    public List<T> content;
    public int number;          // current page index (0-based)
    public int totalPages;
    public long totalElements;
    public boolean first;
    public boolean last;

    public List<T> getContent() {
        return content;
    }

    public boolean isLast() {
        return last;
    }
}
