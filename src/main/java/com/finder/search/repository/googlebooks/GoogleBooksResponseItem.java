package com.finder.search.repository.googlebooks;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleBooksResponseItem {

    private VolumeInfo volumeInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VolumeInfo {
        private String title;
        private List<String> authors;
    }
}
