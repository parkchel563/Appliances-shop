package com.project.appliances.util;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component("urlUtil")
public class UrlUtil {

    public String replacePage(int page, int size) {
        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .replaceQueryParam("page", Math.max(page, 0))
                .replaceQueryParam("size", size)
                .toUriString();
    }

    public String replaceParam(String name, String value) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();

        if (value == null || value.isBlank()) {
            builder.replaceQueryParam(name);
        } else {
            builder.replaceQueryParam(name, value);
        }

        return builder.toUriString();
    }

    public String replaceSort(String sort) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();

        builder.replaceQueryParam("page", 0);

        if (sort == null || sort.isBlank()) {
            builder.replaceQueryParam("sort");
        } else {
            builder.replaceQueryParam("sort", sort);
        }
        return builder.toUriString();
    }

    public String toggleSort(String field) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();

        String currentSort = builder.build().getQueryParams().getFirst("sort");

        String newSort;

        if (currentSort == null || !currentSort.startsWith(field)) {
            newSort = field + ",desc";
        } else if (currentSort.endsWith("desc")) {
            newSort = field + ",asc";
        } else {
            newSort = field + ",desc";
        }

        return builder
                .replaceQueryParam("sort", newSort)
                .replaceQueryParam("page", 0)
                .toUriString();
    }
}
