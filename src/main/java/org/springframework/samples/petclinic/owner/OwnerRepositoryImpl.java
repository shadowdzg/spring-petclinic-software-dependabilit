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
	 * VULNERABLE METHOD - SQL Injection
	 *
	 * This method is vulnerable because it concatenates user input directly into the SQL
	 * query without parameterization.
	 * @param lastName user input that is directly concatenated into SQL
	 * @return list of owners matching the last name
	 */
	@SuppressWarnings("unchecked")
	public List<Owner> findByLastNameVulnerable(String lastName) {
		// VULNERABLE: Direct string concatenation - allows SQL injection
		String sql = "SELECT * FROM owners WHERE last_name = '" + lastName + "'";
		Query query = entityManager.createNativeQuery(sql, Owner.class);
		return query.getResultList();
	}

	/**
	 * VULNERABLE METHOD - SQL Injection with LIKE
	 * @param searchTerm user input that is directly concatenated into SQL
	 * @return list of owners matching the search term
	 */
	@SuppressWarnings("unchecked")
	public List<Owner> searchOwnersVulnerable(String searchTerm) {
		// VULNERABLE: Direct string concatenation in LIKE clause
		String sql = "SELECT * FROM owners WHERE first_name LIKE '%" + searchTerm + "%' OR last_name LIKE '%"
				+ searchTerm + "%' OR city LIKE '%" + searchTerm + "%'";
		Query query = entityManager.createNativeQuery(sql, Owner.class);
		return query.getResultList();
	}

}
