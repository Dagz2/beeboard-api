package org.access.application.dto.command.user;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public record ExtendContractCommand (
    @NotNull(message = "User ID is required") UUID userId,
    @NotNull(message = "New end date is required") @Future(message = "End date should be in the future") LocalDate contractEndDate
) {}