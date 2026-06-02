package ch.policyflow.domain.enums;

/** Lifecycle states of a customer {@code Appointment}. */
public enum AppointmentStatus {
    /** Appointment is booked and in the future. */
    SCHEDULED,
    /** Appointment took place. */
    COMPLETED,
    /** Appointment was cancelled by the advisor or customer. */
    CANCELLED,
    /** Customer did not show up. */
    NO_SHOW
}
