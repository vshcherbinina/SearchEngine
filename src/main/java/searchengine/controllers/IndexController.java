package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.GeneralResponse;
import searchengine.services.IndexService;

@RestController
public class IndexController extends ApiController{
    private final IndexService indexService;

    public IndexController(IndexService indexService) {
        this.indexService = indexService;
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<GeneralResponse> startIndexing() {
        return indexService.startIndexing();
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<GeneralResponse> stopIndexing() {
        return indexService.stopIndexing();
    }

    @PostMapping("/indexPage")
    public ResponseEntity<GeneralResponse> indexPage(String url) {
        return indexService.indexPage(url);
    }

}
