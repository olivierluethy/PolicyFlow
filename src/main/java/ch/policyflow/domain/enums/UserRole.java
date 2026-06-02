package ch.policyflow.domain.enums;

/** Application roles used for JWT-based, role-based access control. */
public enum UserRole {
    /** Full administrative access to every resource. */
    ADMIN,
    /** Insurance advisor: may manage customers, offers, policies and appointments. */
    ADVISOR,
    /** End customer with read-only access to their own data. */
    CUSTOMER
}
