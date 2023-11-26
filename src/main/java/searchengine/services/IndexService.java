package searchengine.services;

import org.springframework.http.ResponseEntity;
import searchengine.dto.GeneralResponse;

public interface IndexService {
    ResponseEntity<GeneralResponse> startIndexing();

    ResponseEntity<GeneralResponse> stopIndexing();

    ResponseEntity<GeneralResponse> indexPage(String url);
}
