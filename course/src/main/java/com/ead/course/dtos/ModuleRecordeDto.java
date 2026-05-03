package com.ead.course.dtos;

import jakarta.validation.constraints.NotBlank;

public record ModuleRecordeDto(
                                @NotBlank String title,
                                @NotBlank String description) {
}
