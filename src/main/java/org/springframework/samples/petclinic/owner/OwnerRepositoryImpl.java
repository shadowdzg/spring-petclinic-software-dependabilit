/*
 * VULNERABLE IMPLEMENTATION - For educational purposes only
 * This class demonstrates SQL Injection vulnerability (OWASP A1: Injection)
 *
 * DO NOT USE IN PRODUCTION - This is intentionally vulnerable code
 */
package org.springframework.samples.petclinic.owner;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.springframework.stereotype.Repository;

/**
 * VULNERABLE: This implementation uses string concatenation in SQL queries, making it
 * susceptible to SQL Injection attacks.
 *
 * Example attack: lastName = "'; DROP TABLE owners; --"
 */
@Repository
public class OwnerRepositoryImpl {

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * SECURE METHOD - SQL Injection Fixed
	 *
	 * This method now uses parameterized queries to prevent SQL injection.
	 * @param lastName user input that is safely parameterized
	 * @return list of owners matching the last name
	 */
	@SuppressWarnings("unchecked")
	public List<Owner> findByLastNameVulnerable(String lastName) {
		// SECURE: Using parameterized query to prevent SQL injection
		String sql = "SELECT * FROM owners WHERE last_name = :lastName";
		Query query = entityManager.createNativeQuery(sql, Owner.class);
		query.setParameter("lastName", lastName);
		return query.getResultList();
	}

	/**
	 * SECURE METHOD - SQL Injection with LIKE Fixed
	 * @param searchTerm user input that is safely parameterized
	 * @return list of owners matching the search term
	 */
	@SuppressWarnings("unchecked")
	public List<Owner> searchOwnersVulnerable(String searchTerm) {
		// SECURE: Using parameterized query to prevent SQL injection
		String sql = "SELECT * FROM owners WHERE first_name LIKE :searchPattern OR last_name LIKE :searchPattern OR city LIKE :searchPattern";
		Query query = entityManager.createNativeQuery(sql, Owner.class);
		query.setParameter("searchPattern", "%" + searchTerm + "%");
		return query.getResultList();
	}

}
