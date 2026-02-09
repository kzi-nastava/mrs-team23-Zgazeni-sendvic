package ZgazeniSendvic.Server_Back_ISS.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.ResponseStatusException;

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

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ex.getMessage());
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Map<String, Object>> handleMultipartException(MultipartException ex) {

        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Invalid multipart request",
                "message", "Request must be sent as multipart/form-data with a file",
                "exception", ex.getClass().getSimpleName()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }


    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(
            ResponseStatusException e,
            HttpServletRequest request) {

        //
        HttpStatus status = HttpStatus.BAD_REQUEST;

        Map<String, String> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", String.valueOf(status.value()),
                "error", status.getReasonPhrase(),
                "message", e.getMessage(),
                "path", request.getRequestURI()
        );

        return ResponseEntity.status(status).body(body);
    }



    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(
            AccessDeniedException e,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.FORBIDDEN; // 403

        Map<String, String> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", String.valueOf(status.value()),
                "error", status.getReasonPhrase(), // "Forbidden"
                "message", e.getMessage(),         //
                "path", request.getRequestURI()
        );

        return ResponseEntity.status(status).body(body);
    }



}
