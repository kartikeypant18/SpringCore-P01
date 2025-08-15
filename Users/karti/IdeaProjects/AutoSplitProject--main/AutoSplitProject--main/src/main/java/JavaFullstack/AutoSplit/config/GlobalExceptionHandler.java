package JavaFullstack.AutoSplit.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException ex) {
        Map<String,Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Bad Request",
                "message", ex.getMessage()
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex) {
        Map<String,Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", "Server Error",
                "message", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
