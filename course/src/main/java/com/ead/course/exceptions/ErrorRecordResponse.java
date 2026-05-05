package com.ead.course.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorRecordResponse(
        int  status,
        String message,
        String path,
        OffsetDateTime timestamp,
        Map<String, String> errorDetails) {
}
