package searchengine.dto.statistics;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import searchengine.dto.GeneralResponse;

@Getter
@Setter
@NoArgsConstructor
public class StatisticsResponse extends GeneralResponse {
    private final StatisticsData statistics = new StatisticsData();

    public ResponseEntity<StatisticsResponse> toSuccessResponse() {
        setSuccessStatus();
        return statisticsToResponseEntity();
    }

    public ResponseEntity<StatisticsResponse> toErrorResponse(Exception e, String message) {
        handleError(e, message);
        return statisticsToResponseEntity();
    }

    public ResponseEntity<StatisticsResponse> statisticsToResponseEntity() {
        return new ResponseEntity<>(this, getHttpStatus());
    }

}





