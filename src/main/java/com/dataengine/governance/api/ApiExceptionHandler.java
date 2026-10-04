package com.dataengine.governance.api;

import com.dataengine.governance.service.GovernanceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(GovernanceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> governance(GovernanceException ex) {
        return Map.of("timestamp", Instant.now(), "status", 409, "error", "Governance rule blocked the action", "message", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream().findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage()).orElse("请求字段无效");
        return Map.of("timestamp", Instant.now(), "status", 400, "error", "Validation failed", "message", message);
    }
}
