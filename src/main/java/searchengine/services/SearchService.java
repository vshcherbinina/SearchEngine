package searchengine.services;

import org.springframework.http.ResponseEntity;
import searchengine.dto.search.SearchResponse;

public interface SearchService {
    ResponseEntity<SearchResponse> getSearchResult(String query, int offset, int limit, String site);
}
