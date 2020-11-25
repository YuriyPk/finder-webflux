package com.finder.search;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SearchResultItem {
    private String title;
    private List<String> authors;
    private SearchResultItemType type;
}
