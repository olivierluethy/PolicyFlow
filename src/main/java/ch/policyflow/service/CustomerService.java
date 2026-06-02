package ch.policyflow.service;

import ch.policyflow.domain.entity.AuditLog;
import ch.policyflow.domain.entity.Customer;
import ch.policyflow.domain.entity.Policy;
import ch.policyflow.domain.enums.PolicyStatus;
import ch.policyflow.dto.request.CreateCustomerRequest;
import ch.policyflow.dto.request.UpdateCustomerRequest;
import ch.policyflow.exception.BusinessException;
import ch.policyflow.exception.ResourceNotFoundException;
import ch.policyflow.security.CurrentUser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

/**
 * Business logic for managing customers. Every mutation is recorded in the audit log.
 */
@ApplicationScoped
public class CustomerService {

    @Inject
    CurrentUser currentUser;

    /**
     * Lists customers, optionally filtered by a free-text search term.
     *
     * @param search a name/email search term, or {@code null}/blank for all customers
     * @return matching active customers
     */
    public List<Customer> list(String search) {
        if (search == null || search.isBlank()) {
            return Customer.findAllActive();
        }
        return Customer.searchByName(search.trim());
    }

    /**
     * Loads a single customer by id.
     *
     * @param id the customer id
     * @return the customer
     * @throws ResourceNotFoundException if no active customer has that id
     */
    public Customer get(Long id) {
        Customer customer = Customer.findById(id);
        if (customer == null || !customer.active) {
            throw new ResourceNotFoundException("Customer", id);
        }
        return customer;
    }

    /**
     * Creates a new customer.
     *
     * @param request the validated creation payload
     * @return the persisted customer
     * @throws BusinessException if the email is already in use
     */
    @Transactional
    public Customer create(CreateCustomerRequest request) {
        if (Customer.findByEmail(request.email()) != null) {
            throw new BusinessException("A customer with email '" + request.email() + "' already exists");
        }
        Customer customer = new Customer();
        customer.firstName = request.firstName();
        customer.lastName = request.lastName();
        customer.email = request.email();
        customer.phone = request.phone();
        customer.dateOfBirth = request.dateOfBirth();
        customer.canton = request.canton();
        customer.address = request.address();
        customer.city = request.city();
        customer.zip = request.zip();
        customer.persist();

        AuditLog.log("Customer", customer.id, "CREATE",
                "Created customer " + fullName(customer), currentUser.username());
        return customer;
    }

    /**
     * Updates an existing customer.
     *
     * @param id      the customer id
     * @param request the validated update payload
     * @return the updated customer
     * @throws ResourceNotFoundException if the customer does not exist
     * @throws BusinessException         if the new email is already used by another customer
     */
    @Transactional
    public Customer update(Long id, UpdateCustomerRequest request) {
        Customer customer = get(id);

        Customer byEmail = Customer.findByEmail(request.email());
        if (byEmail != null && !byEmail.id.equals(customer.id)) {
            throw new BusinessException("A customer with email '" + request.email() + "' already exists");
        }

        customer.firstName = request.firstName();
        customer.lastName = request.lastName();
        customer.email = request.email();
        customer.phone = request.phone();
        customer.dateOfBirth = request.dateOfBirth();
        customer.canton = request.canton();
        customer.address = request.address();
        customer.city = request.city();
        customer.zip = request.zip();

        AuditLog.log("Customer", customer.id, "UPDATE",
                "Updated customer " + fullName(customer), currentUser.username());
        return customer;
    }

    /**
     * Soft-deletes a customer, provided they hold no active policies.
     *
     * @param id the customer id
     * @throws ResourceNotFoundException if the customer does not exist
     * @throws BusinessException         if the customer still has active policies
     */
    @Transactional
    public void delete(Long id) {
        Customer customer = get(id);

        long activePolicies = Policy.count("customer.id = ?1 and status = ?2", id, PolicyStatus.ACTIVE);
        if (activePolicies > 0) {
            throw new BusinessException(
                    "Cannot delete customer with " + activePolicies + " active polic(y/ies)");
        }

        customer.active = false;
        AuditLog.log("Customer", customer.id, "DELETE",
                "Deleted customer " + fullName(customer), currentUser.username());
    }

    private String fullName(Customer c) {
        return c.firstName + " " + c.lastName;
    }
}
