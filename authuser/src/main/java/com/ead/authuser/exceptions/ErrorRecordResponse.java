package com.ead.authuser.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorRecordResponse(
        int  status,
        String message,
        String path,
        LocalDateTime timestamp,
        Map<String, String> errorDetails) {
}
