package com.finder.search;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.finder.search.repository.RemoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SearchService {

    private final List<RemoteRepository> remoteRepositories;

    List<SearchResultItem> findByTerm(String term) {
        List<Future<List<SearchResultItem>>> remoteResults = remoteRepositories.stream()
                                                                               .map(repository -> repository.findByTerm(term))
                                                                               .collect(Collectors.toList());

        return remoteResults.stream()
                            .flatMap(this::retrieveResults)
                            .peek(resultItem -> log.debug(resultItem.toString()))
                            .filter(resultItem -> resultItem.getTitle() != null)
                            .sorted(Comparator.comparing(SearchResultItem::getTitle))
                            .collect(Collectors.toList());
    }

    private Stream<SearchResultItem> retrieveResults(Future<List<SearchResultItem>> remoteResult) {
        try {
            return remoteResult.get().stream();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Retrieving remote results error: ", e);
            return Stream.empty();
        }
    }
}
