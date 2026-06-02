package ch.policyflow.domain.enums;

/**
 * The three statutory age groups used to price Swiss mandatory health insurance.
 *
 * <ul>
 *   <li>{@code KIND} – children (0–18), heavily subsidised (factor 0.55)</li>
 *   <li>{@code JUNGER_ERWACHSENER} – young adults (19–25), reduced (factor 0.85)</li>
 *   <li>{@code ERWACHSENER} – adults (26+), full premium (factor 1.00)</li>
 * </ul>
 */
public enum AgeGroup {

    KIND("Kind", 0.55),
    JUNGER_ERWACHSENER("Junger Erwachsener", 0.85),
    ERWACHSENER("Erwachsener", 1.00);

    private final String label;
    private final double ageFactor;

    AgeGroup(String label, double ageFactor) {
        this.label = label;
        this.ageFactor = ageFactor;
    }

    /** @return the human-readable German label for this age group. */
    public String getLabel() {
        return label;
    }

    /** @return the premium multiplier applied for this age group. */
    public double getAgeFactor() {
        return ageFactor;
    }

    /**
     * Determines the age group for a given age.
     *
     * @param age the insured person's age in years
     * @return the matching {@link AgeGroup}
     * @throws IllegalArgumentException if the age is negative
     */
    public static AgeGroup fromAge(int age) {
        if (age < 0) {
            throw new IllegalArgumentException("Age must not be negative: " + age);
        }
        if (age <= 18) {
            return KIND;
        }
        if (age <= 25) {
            return JUNGER_ERWACHSENER;
        }
        return ERWACHSENER;
    }
}
