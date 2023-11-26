package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.search.SearchResponse;
import searchengine.services.SearchService;

@RestController
public class SearchController extends ApiController{
    private static final int RESULT_LIMIT_DEFAULT = 20;
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(String query,
                                                 @RequestParam(required = false) int offset,
                                                 @RequestParam(required = false) int limit,
                                                 @RequestParam(required = false) String site)
    {
        return searchService.getSearchResult(query, offset,
                limit <= 0 ? RESULT_LIMIT_DEFAULT : limit,
                site == null ? "" : site);
    }
}
