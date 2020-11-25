package com.finder.search;

import java.util.List;

import com.finder.search.repository.googlebooks.GoogleBooksRepository;
import com.finder.search.repository.itunes.ITunesRepository;
import com.finder.search.repository.RemoteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SearchConfiguration implements WebMvcConfigurer {

    @Value("${repository.resultsLimit:5}")
    private int resultsLimit;

    @Bean
    public SearchService searchService(List<RemoteRepository> remoteRepositories) {
        return new SearchService(remoteRepositories);
    }

    @Bean
    public ITunesRepository iTunesRepository(RetryTemplate retryTemplate,
                                             RestTemplate restTemplate,
                                             @Value("${repository.iTunes.url}") String url) {
        return new ITunesRepository(retryTemplate, restTemplate, url, resultsLimit);
    }

    @Bean
    public GoogleBooksRepository googleBooksRepository(RetryTemplate retryTemplate,
                                                       RestTemplate restTemplate,
                                                       @Value("${repository.googleBooks.url}") String url) {
        return new GoogleBooksRepository(retryTemplate, restTemplate, url, resultsLimit);
    }

    @Bean
    public RetryTemplate retryTemplate(@Value("${repository.retry.maxAttempts:3}") int maxAttempts,
                                       @Value("${repository.retry.backOffPeriod:1000}") long backOffPeriod) {
        var retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxAttempts);
        var backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(backOffPeriod);
        var template = new RetryTemplate();
        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOffPolicy);
        return template;
    }

    @Bean
    public RestTemplate restTemplate() {
        var messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setSupportedMediaTypes(List.of(MediaType.parseMediaType("text/javascript; charset=utf-8"),
                                                        MediaType.parseMediaType("application/json; charset=utf-8")));
        var restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(List.of(messageConverter));
        return restTemplate;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("search");
    }
}
