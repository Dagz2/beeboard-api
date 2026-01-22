package org.shared.domain;

/**
 * Published Language: Account status for access control.
 *
 * Derived from User entity state in BC Access:
 * - ACTIVE: !isLocked() && !isContractExpired()
 * - LOCKED: isLocked() == true
 * - EXPIRED: isContractExpired() == true
 */
public enum AccountStatus {

    /**
     * Account is fully operational.
     * User can authenticate and access all permitted resources.
     */
    ACTIVE,

    /**
     * Account is administratively locked.
     * User cannot authenticate until unlocked by an administrator.
     */
    LOCKED,

    /**
     * Contract has expired.
     * User cannot authenticate until contract is renewed.
     */
    EXPIRED;

    /**
     * Checks if the user can authenticate with this status.
     */
    public boolean canAuthenticate() {
        return this == ACTIVE;
    }
}
