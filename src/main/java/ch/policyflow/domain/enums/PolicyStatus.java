package ch.policyflow.domain.enums;

/** Lifecycle states of an issued {@code Policy}. */
public enum PolicyStatus {
    /** Policy is in force. */
    ACTIVE,
    /** Policy was terminated before its natural end date. */
    CANCELLED,
    /** Policy reached its end date and is no longer in force. */
    EXPIRED,
    /** Policy is temporarily inactive (e.g. unpaid premiums). */
    SUSPENDED
}
