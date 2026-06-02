package ch.policyflow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.policyflow.domain.entity.AuditLog;
import ch.policyflow.domain.entity.Customer;
import ch.policyflow.domain.enums.Canton;
import ch.policyflow.dto.request.CreateCustomerRequest;
import ch.policyflow.exception.BusinessException;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration-style unit tests for {@link CustomerService} running against the
 * Dev Services PostgreSQL instance with the seeded data set.
 */
@QuarkusTest
class CustomerServiceTest {

    @Inject
    CustomerService customerService;

    @Test
    @TestTransaction
    @DisplayName("Creating a customer persists it and writes a CREATE audit entry")
    void createWritesAuditEntry() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Test", "Person", "test.person@example.ch", "+41 79 000 00 00",
                LocalDate.of(1991, 5, 5), Canton.ZG, "Teststrasse 1", "Zug", "6300");

        Customer created = customerService.create(request);

        assertTrue(created.id != null && created.id > 0, "customer should be persisted with an id");
        long auditCount = AuditLog.count("entityType = ?1 and entityId = ?2 and action = ?3",
                "Customer", created.id, "CREATE");
        assertEquals(1, auditCount, "exactly one CREATE audit entry expected");
    }

    @Test
    @TestTransaction
    @DisplayName("Deleting a customer with an active policy throws BusinessException")
    void deleteWithActivePolicyThrows() {
        // Seed data: customer 1 (Anna Müller) holds an active policy.
        assertThrows(BusinessException.class, () -> customerService.delete(1L));
    }

    @Test
    @TestTransaction
    @DisplayName("Deleting a customer without policies soft-deletes them")
    void deleteWithoutPoliciesSoftDeletes() {
        // Seed customer 9 (Sophie Meier) has no policies.
        customerService.delete(9L);
        Customer c = Customer.findById(9L);
        assertFalse(c.active, "customer should be flagged inactive");
    }

    @Test
    @DisplayName("Search matches seeded customers by name")
    void searchReturnsResults() {
        List<Customer> results = customerService.list("Müller");
        assertTrue(results.stream().anyMatch(c -> c.email.equals("anna.mueller@email.ch")),
                "search for 'Müller' should find Anna Müller");
    }
}
