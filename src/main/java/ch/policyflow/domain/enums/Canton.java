package ch.policyflow.domain.enums;

/**
 * The 26 Swiss cantons.
 *
 * <p>Each canton carries a {@code regionalFactor} that reflects the relative cost of
 * healthcare in that region. In the Swiss mandatory health insurance system
 * (Krankenpflegeversicherung, KVG) premiums vary significantly by canton because
 * healthcare costs, hospital density and demographics differ across the country.
 * Urban cantons such as Basel-Stadt (BS), Geneva (GE) and Vaud (VD) are the most
 * expensive, while rural cantons such as Uri (UR) and Appenzell Innerrhoden (AI)
 * are the cheapest.</p>
 */
public enum Canton {

    ZH("Zürich", 1.18),
    BE("Bern", 1.05),
    LU("Luzern", 1.02),
    UR("Uri", 0.95),
    SZ("Schwyz", 1.00),
    OW("Obwalden", 0.96),
    NW("Nidwalden", 0.97),
    GL("Glarus", 0.98),
    ZG("Zug", 1.08),
    FR("Freiburg", 1.06),
    SO("Solothurn", 1.04),
    BS("Basel-Stadt", 1.22),
    BL("Basel-Landschaft", 1.15),
    SH("Schaffhausen", 1.03),
    AR("Appenzell Ausserrhoden", 0.99),
    AI("Appenzell Innerrhoden", 0.97),
    SG("St. Gallen", 1.08),
    GR("Graubünden", 0.98),
    AG("Aargau", 1.06),
    TG("Thurgau", 1.01),
    TI("Tessin", 1.12),
    VD("Waadt", 1.20),
    VS("Wallis", 1.05),
    NE("Neuenburg", 1.10),
    GE("Genf", 1.25),
    JU("Jura", 1.08);

    private final String fullName;
    private final double regionalFactor;

    Canton(String fullName, double regionalFactor) {
        this.fullName = fullName;
        this.regionalFactor = regionalFactor;
    }

    /** @return the canton's full German name (e.g. "Zürich"). */
    public String getFullName() {
        return fullName;
    }

    /** @return the regional premium multiplier applied during premium calculation. */
    public double getRegionalFactor() {
        return regionalFactor;
    }
}
