package org.access.domain.exception;

import org.access.domain.model.SecurityTier;

/**
 * Exception thrown when an actor does not have sufficient tier level to perform an operation.
 */
public class InsufficientTierException extends SecurityException {

    public InsufficientTierException(String message) {
        super(message);
    }

    public static InsufficientTierException requiresTier(SecurityTier requiredTier, SecurityTier actualTier) {
        return new InsufficientTierException(
            String.format("This operation requires tier %s or higher. Current tier: %s",
                requiredTier.name(), actualTier.name())
        );
    }

    public static InsufficientTierException onlyTierZeroCanPerform(String operation) {
        return new InsufficientTierException(
            String.format("Only SUPER_ADMIN (TIER_0) can perform: %s", operation)
        );
    }
}
