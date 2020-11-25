package com.finder.search.repository;

import java.util.List;
import java.util.concurrent.Future;

import com.finder.search.SearchResultItem;

public interface RemoteRepository {

    Future<List<SearchResultItem>> findByTerm(String term);
}
