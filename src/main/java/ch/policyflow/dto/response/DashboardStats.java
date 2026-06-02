package ch.policyflow.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregated key figures shown on the dashboard.
 *
 * @param totalCustomers        number of active customers
 * @param activeOffers          number of pending offers
 * @param activePolicies        number of active policies
 * @param appointmentsThisWeek  scheduled appointments within the next 7 days
 * @param premiumVolumeMonthly  summed monthly premium of all active policies
 * @param recentActivity        the five most recent audit entries
 * @param upcomingAppointments  the next three upcoming appointments
 */
public record DashboardStats(
        long totalCustomers,
        long activeOffers,
        long activePolicies,
        long appointmentsThisWeek,
        BigDecimal premiumVolumeMonthly,
        List<AuditLogResponse> recentActivity,
        List<AppointmentResponse> upcomingAppointments) {
}
