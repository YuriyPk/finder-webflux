package com.finder.search;

import java.util.List;

import javax.validation.constraints.NotBlank;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
public class SearchController {

    private final SearchService searchService;

    @GetMapping(path="/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<SearchResultItem> search(@RequestParam @NotBlank String term) {
        log.info("Start search by term: " + term);
        List<SearchResultItem> results = searchService.findByTerm(term);
        log.info("Total results count: " + results.size());
        return Flux.fromIterable(results);
    }
}
