package ch.policyflow.domain.enums;

/** Lifecycle states of an insurance {@code Offer}. */
public enum OfferStatus {
    /** Offer has been created and is awaiting the customer's decision. */
    PENDING,
    /** Customer accepted the offer; a policy has been (or will be) issued. */
    ACCEPTED,
    /** Customer declined the offer. */
    REJECTED,
    /** Offer was not acted on within its validity window and has lapsed. */
    EXPIRED
}
