package ch.policyflow.domain.enums;

/**
 * The statutory deductible ("Franchise") levels available in Swiss mandatory
 * health insurance for adults.
 *
 * <p>By law a policyholder may choose an annual deductible between CHF 300 and
 * CHF 2500. A higher deductible means the insured pays more out of pocket before
 * the insurer contributes, and is therefore rewarded with a lower monthly premium.
 * The {@code discountFactor} encodes that reward: it is the fraction of the base
 * premium that remains payable at the chosen deductible level.</p>
 */
public enum Franchise {

    CHF_300(300, 1.00),
    CHF_500(500, 0.95),
    CHF_1000(1000, 0.86),
    CHF_1500(1500, 0.79),
    CHF_2000(2000, 0.73),
    CHF_2500(2500, 0.68);

    private final int amount;
    private final double discountFactor;

    Franchise(int amount, double discountFactor) {
        this.amount = amount;
        this.discountFactor = discountFactor;
    }

    /** @return the deductible amount in Swiss francs. */
    public int getAmount() {
        return amount;
    }

    /** @return the premium multiplier (≤ 1.0) granted for this deductible level. */
    public double getDiscountFactor() {
        return discountFactor;
    }

    /**
     * Resolves a {@code Franchise} from its raw franc amount.
     *
     * @param amount the deductible amount (e.g. 1000)
     * @return the matching enum constant
     * @throws IllegalArgumentException if the amount is not a valid statutory franchise
     */
    public static Franchise fromAmount(int amount) {
        for (Franchise f : values()) {
            if (f.amount == amount) {
                return f;
            }
        }
        throw new IllegalArgumentException(
                "Invalid franchise amount: " + amount + ". Allowed: 300, 500, 1000, 1500, 2000, 2500.");
    }
}
