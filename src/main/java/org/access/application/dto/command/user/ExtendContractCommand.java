package org.access.application.dto.command.user;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public record ExtendContractCommand (
    @NotNull UUID userId,
    @NotNull @Future LocalDate newEndDate
) {}