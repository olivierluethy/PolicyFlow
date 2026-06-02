package ch.policyflow.resource;

import ch.policyflow.dto.request.PremiumCalculationRequest;
import ch.policyflow.dto.request.PremiumCompareRequest;
import ch.policyflow.dto.response.CalculationResult;
import ch.policyflow.service.PremiumCalculatorService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/**
 * Premium calculation endpoints. The calculator is also usable standalone, without a
 * customer or offer.
 */
@Path("/api/premium")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "ADVISOR"})
public class PremiumResource {

    @Inject
    PremiumCalculatorService calculator;

    /**
     * Calculates a single premium.
     *
     * @param request the calculation inputs
     * @return the calculation breakdown
     */
    @POST
    @Path("/calculate")
    public CalculationResult calculate(@Valid PremiumCalculationRequest request) {
        return calculator.calculate(request);
    }

    /**
     * Compares the premium across all six deductible levels.
     *
     * @param request the comparison inputs
     * @return one result per franchise level
     */
    @POST
    @Path("/compare")
    public List<CalculationResult> compare(@Valid PremiumCompareRequest request) {
        return calculator.compare(request);
    }
}
