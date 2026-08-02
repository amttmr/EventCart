package com.eventcart.common.web;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Stable pagination response returned by APIs that support paging.
 *
 * <p>This record decouples public API responses from Spring Data's {@link Page}
 * implementation details while preserving the pagination values clients need.</p>
 *
 * @param content page content
 * @param page zero-based page number
 * @param size requested page size
 * @param totalElements total matching elements
 * @param totalPages total number of pages
 * @param first whether this is the first page
 * @param last whether this is the last page
 * @param <T> content item type
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    /**
     * Converts a Spring Data page into a public pagination response.
     *
     * @param page Spring Data page returned by a repository or service
     * @param <T> content item type
     * @return API-friendly pagination response
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
