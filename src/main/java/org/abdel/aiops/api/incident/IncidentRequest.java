package org.abdel.aiops.api.incident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IncidentRequest(@NotBlank(message = "service is required")
                               @Size(
                                       max = 100,
                                       message = "service must not exceed 100 characters"
                               )
                               String service,

                              @NotBlank(message = "stackTrace is required")
                               @Size(
                                       max = 50_000,
                                       message = "stackTrace must not exceed 50000 characters"
                               )
                               String stackTrace) {
}
