package searchengine.dto.search;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import searchengine.dto.GeneralResponse;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SearchResponse extends GeneralResponse {
    private int count;
    private List<SearchData> data = new ArrayList<>();

    public ResponseEntity<SearchResponse> toSuccessResponse() {
        setSuccessStatus();
        return searchToResponseEntity();
    }

    public ResponseEntity<SearchResponse> toErrorResponse(Exception e, String message) {
        handleError(e, message);
        return searchToResponseEntity();
    }

    public ResponseEntity<SearchResponse> toErrorResponse(HttpStatus httpStatus, String message) {
        setErrorStatus(httpStatus, message);
        return searchToResponseEntity();
    }

    public ResponseEntity<SearchResponse> searchToResponseEntity() {
        return new ResponseEntity<>(this, getHttpStatus());
    }

}

