package org.access.exposition.security;

/**
 * Logical operator for combining multiple permission requirements.
 */
public enum LogicalOperator {

    /**
     * User must have at least one of the specified permissions.
     */
    OR,

    /**
     * User must have all of the specified permissions.
     */
    AND
}
