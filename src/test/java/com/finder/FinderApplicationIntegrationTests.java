package com.finder;

import java.util.List;

import com.finder.search.SearchResultItemType;
import com.finder.search.repository.googlebooks.GoogleBooksResponse;
import com.finder.search.repository.googlebooks.GoogleBooksResponseItem;
import com.finder.search.repository.itunes.ITunesResponse;
import com.finder.search.repository.itunes.ITunesResponseItem;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FinderApplicationIntegrationTests {

    private static final String SEARCH_URL = "/search?term={term}";
    private static final String SEARCH_TERM = "test";
    private static final String BOOK_TITLE = "Book title";
    private static final String BOOK_AUTHOR = "Book author";
    private static final String ALBUM_TITLE = "Album title";
    private static final String ALBUM_AUTHOR = "Album author";

    private static final String JSON_RESULT_ITEM_TEMPLATE = "{\"title\":\"%s\",\"authors\":[\"%s\"],\"type\":\"%s\"}";
    private static final String BOOK_JSON_RESULT = String.format(JSON_RESULT_ITEM_TEMPLATE, BOOK_TITLE, BOOK_AUTHOR, SearchResultItemType.BOOK);
    private static final String ALBUM_JSON_RESULT = String.format(JSON_RESULT_ITEM_TEMPLATE, ALBUM_TITLE, ALBUM_AUTHOR, SearchResultItemType.ALBUM);

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockBean
    private RestTemplate restTemplate;

    @Value("${repository.googleBooks.url}")
    private String gogleBooksUrl;
    @Value("${repository.iTunes.url}")
    private String iTunesUrl;
    @Value("${repository.resultsLimit}")
    private int resultsLimit;
    @Value("${repository.retry.maxAttempts}")
    private int maxAttempts;

    @Test
    public void testSearch() {
        when(restTemplate.getForObject(gogleBooksUrl, GoogleBooksResponse.class, resultsLimit, SEARCH_TERM))
                .thenReturn(getGoogleBooksResponse());
        when(restTemplate.getForObject(iTunesUrl, ITunesResponse.class, resultsLimit, SEARCH_TERM))
                .thenReturn(getITunesResponse());

        ResponseEntity results = testRestTemplate.exchange(SEARCH_URL, HttpMethod.GET, HttpEntity.EMPTY, String.class, SEARCH_TERM);

        assertEquals("[" + ALBUM_JSON_RESULT + "," + BOOK_JSON_RESULT + "]", results.getBody());
    }

    @Test
    public void testSearchWithGoogleBooksRetry() {
        when(restTemplate.getForObject(gogleBooksUrl, GoogleBooksResponse.class, resultsLimit, SEARCH_TERM))
                .thenThrow(new RuntimeException());
        when(restTemplate.getForObject(iTunesUrl, ITunesResponse.class, resultsLimit, SEARCH_TERM))
                .thenReturn(getITunesResponse());

        ResponseEntity results = testRestTemplate.exchange(SEARCH_URL, HttpMethod.GET, HttpEntity.EMPTY, String.class, SEARCH_TERM);

        assertEquals("[" + ALBUM_JSON_RESULT + "]", results.getBody());

        verify(restTemplate, times(maxAttempts)).getForObject(gogleBooksUrl, GoogleBooksResponse.class, resultsLimit, SEARCH_TERM);
    }

    @Test
    public void testSearchWithITunesRetry() {
        when(restTemplate.getForObject(gogleBooksUrl, GoogleBooksResponse.class, resultsLimit, SEARCH_TERM))
                .thenReturn(getGoogleBooksResponse());
        when(restTemplate.getForObject(iTunesUrl, ITunesResponse.class, resultsLimit, SEARCH_TERM))
                .thenThrow(new RuntimeException());

        ResponseEntity results = testRestTemplate.exchange(SEARCH_URL, HttpMethod.GET, HttpEntity.EMPTY, String.class, SEARCH_TERM);

        assertEquals("[" + BOOK_JSON_RESULT + "]", results.getBody());

        verify(restTemplate, times(maxAttempts)).getForObject(iTunesUrl, ITunesResponse.class, resultsLimit, SEARCH_TERM);
    }

    private GoogleBooksResponse getGoogleBooksResponse() {
        var responseItem = new GoogleBooksResponseItem(new GoogleBooksResponseItem.VolumeInfo(BOOK_TITLE, List.of(BOOK_AUTHOR)));
        return new GoogleBooksResponse(List.of(responseItem));
    }

    private ITunesResponse getITunesResponse() {
        var responseItem = new ITunesResponseItem(ALBUM_AUTHOR, ALBUM_TITLE);
        return new ITunesResponse(List.of(responseItem));
    }
}
