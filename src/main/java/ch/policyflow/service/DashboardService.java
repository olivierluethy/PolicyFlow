package ch.policyflow.service;

import ch.policyflow.domain.entity.Appointment;
import ch.policyflow.domain.entity.AuditLog;
import ch.policyflow.domain.entity.Customer;
import ch.policyflow.domain.entity.Offer;
import ch.policyflow.domain.entity.Policy;
import ch.policyflow.domain.enums.OfferStatus;
import ch.policyflow.domain.enums.PolicyStatus;
import ch.policyflow.dto.response.AppointmentResponse;
import ch.policyflow.dto.response.AuditLogResponse;
import ch.policyflow.dto.response.DashboardStats;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregates portfolio-wide key figures for the dashboard view.
 */
@ApplicationScoped
public class DashboardService {

    private static final int UPCOMING_WINDOW_DAYS = 7;
    private static final int RECENT_ACTIVITY_LIMIT = 5;
    private static final int UPCOMING_LIMIT = 3;

    /**
     * Computes the dashboard statistics in a single call.
     *
     * @return the aggregated {@link DashboardStats}
     */
    public DashboardStats getStats() {
        long totalCustomers = Customer.count("active = true");
        long activeOffers = Offer.count("status = ?1", OfferStatus.PENDING);
        long activePolicies = Policy.count("status = ?1", PolicyStatus.ACTIVE);
        long appointmentsThisWeek = Appointment.findUpcoming(UPCOMING_WINDOW_DAYS).size();

        BigDecimal premiumVolume = Policy.<Policy>list("status = ?1", PolicyStatus.ACTIVE).stream()
                .map(p -> p.monthlyPremium)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AuditLogResponse> recent = AuditLog.findRecent(RECENT_ACTIVITY_LIMIT).stream()
                .map(AuditLogResponse::from)
                .toList();

        List<AppointmentResponse> upcoming = Appointment.findUpcoming(UPCOMING_WINDOW_DAYS).stream()
                .limit(UPCOMING_LIMIT)
                .map(AppointmentResponse::from)
                .toList();

        return new DashboardStats(totalCustomers, activeOffers, activePolicies,
                appointmentsThisWeek, premiumVolume, recent, upcoming);
    }
}
