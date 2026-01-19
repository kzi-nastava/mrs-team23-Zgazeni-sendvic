package ZgazeniSendvic.Server_Back_ISS.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleEmailConflict(IllegalStateException e, HttpServletRequest request){
        Map<String,String> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", "409",
                "error", "Conflict",
                "message", e.getMessage(),
                "path", request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);

    }

}
