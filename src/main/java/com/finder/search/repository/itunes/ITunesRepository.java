package com.finder.search.repository.itunes;

import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.finder.search.repository.RemoteRepository;
import com.finder.search.SearchResultItemType;
import com.finder.search.SearchResultItem;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
public class ITunesRepository implements RemoteRepository {

    private final RetryTemplate retryTemplate;
    private final RestTemplate restTemplate;
    private final String url;
    private final int resultsLimit;

    @HystrixCommand(commandKey = "iTunesSearch", fallbackMethod = "fallback")
    public Future<List<SearchResultItem>> findByTerm(String term) {
        return new AsyncResult<>() {
            @Override
            public List<SearchResultItem> invoke() {
                ITunesResponse response = retryTemplate.execute(ctx -> {
                    log.info("Search by term: " + term);
                    return restTemplate.getForObject(url, ITunesResponse.class, resultsLimit, term);
                });

                List<SearchResultItem> searchResultItems = convertToSearchResultItems(response);
                log.info("Results count: " + searchResultItems.size());

                return searchResultItems;
            }
        };
    }

    private List<SearchResultItem> convertToSearchResultItems(ITunesResponse response) {
        if (response == null || response.getResults() == null) {
            return List.of();
        }
        return response.getResults().stream()
                       .peek(responseItem -> log.debug(responseItem.toString()))
                       .map(responseItem -> SearchResultItem.builder()
                                                            .title(responseItem.getCollectionName())
                                                            .authors(List.of(responseItem.getArtistName()))
                                                            .type(SearchResultItemType.ALBUM)
                                                            .build())
                       .collect(Collectors.toList());
    }

    private List<SearchResultItem> fallback(String term) {
        log.warn("Fallback logic is processed");
        return List.of();
    }
}
