package searchengine.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.JDBCException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

@Slf4j
@Data
@NoArgsConstructor
public class GeneralResponse {
    public final static HttpStatus DEFAULT_ERROR_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;
    private boolean result;
    private String error;
    @JsonIgnore
    private HttpStatus httpStatus;

    public static ResponseEntity<GeneralResponse> newSuccessResponse() {
        GeneralResponse generalResponse = new GeneralResponse();
        generalResponse.setSuccessStatus();
        return generalResponse.generalToResponseEntity();
    }

    public static ResponseEntity<GeneralResponse> newErrorResponse(Exception e, String message) {
        GeneralResponse response = new GeneralResponse();
        response.handleError(e, message);
        return response.generalToResponseEntity();
    }

    public static ResponseEntity<GeneralResponse> newErrorResponse(HttpStatus httpStatus, String message) {
        GeneralResponse response = new GeneralResponse();
        response.setErrorStatus(httpStatus, message);
        return response.generalToResponseEntity();
    }

    public void setSuccessStatus() {
        result = true;
        httpStatus = HttpStatus.OK;
    }

    public void setErrorStatus(HttpStatus httpStatus, String message) {
        result = false;
        this.httpStatus = (httpStatus == null ? DEFAULT_ERROR_STATUS : httpStatus);
        this.error = message;
        log.error(message);
    }

    public void handleError(Exception exception, String message) {
        final String DEV = ": ";
        StringBuilder errorBuilder = new StringBuilder();
        HttpStatus status;
        if (message != null && !message.isBlank()) {
            errorBuilder.append(message).append(DEV);
        }
        if (exception.getCause() instanceof JDBCException e) {
            errorBuilder.append("Ошибка подключения к базе данных").append(DEV).append(e.getSQLException().getMessage());
            status = HttpStatus.FORBIDDEN;
        } else if (exception.getCause() instanceof IllegalArgumentException e) {
            errorBuilder.append(e.getMessage());
            status = HttpStatus.METHOD_NOT_ALLOWED;
        } else {
            errorBuilder.append(exception.getMessage());
            status = DEFAULT_ERROR_STATUS;
        }
        setErrorStatus(status, errorBuilder.toString());
        Arrays.stream(exception.getStackTrace()).toList().forEach(traceElement -> log.error('\t' + traceElement.toString()));
    }

    public ResponseEntity<GeneralResponse> generalToResponseEntity() {
        return new ResponseEntity<>(this, getHttpStatus());
    }
}
