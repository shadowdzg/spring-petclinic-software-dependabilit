/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.Visit;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for JML-annotated methods in {@link Owner} Tests the formal specifications
 * implemented with JML annotations
 *
 * @author Aymen Altair
 */
class OwnerJMLTests {

	private Owner owner;

	private Pet pet;

	private PetType petType;

	@BeforeEach
	void setUp() {
		owner = new Owner();
		owner.setFirstName("John");
		owner.setLastName("Doe");
		owner.setAddress("123 Main St");
		owner.setCity("Springfield");
		owner.setTelephone("1234567890");

		petType = new PetType();
		petType.setName("Dog");

		pet = new Pet();
		pet.setName("Buddy");
		pet.setType(petType);
		pet.setBirthDate(LocalDate.now().minusYears(2));
	}

	@Test
	void testAddPetJMLPreconditions() {
		// Test JML precondition: pet != null
		int initialSize = owner.getPets().size();

		// Add a new pet (satisfies pet.isNew() precondition)
		owner.addPet(pet);

		// Test JML postconditions
		assertThat(owner.getPets()).contains(pet);
		assertThat(owner.getPets().size()).isEqualTo(initialSize + 1);
	}

	@Test
	void testAddPetDoesNotAddExistingPet() {
		// Set pet ID to simulate non-new pet
		pet.setId(1);
		int initialSize = owner.getPets().size();

		// Should not add pet that is not new
		owner.addPet(pet);

		assertThat(owner.getPets().size()).isEqualTo(initialSize);
		assertThat(owner.getPets()).doesNotContain(pet);
	}

	@Test
	void testGetPetByNameJMLSpecification() {
		owner.addPet(pet);

		// Test JML postcondition: result matches name or is null
		Pet foundPet = owner.getPet("Buddy");
		assertThat(foundPet).isNotNull();
		assertThat(foundPet.getName()).isEqualToIgnoringCase("Buddy");

		// Test with non-existent name
		Pet notFound = owner.getPet("NonExistent");
		assertThat(notFound).isNull();
	}

	@Test
	void testGetPetByIdJMLSpecification() {
		// First add pet without ID (new pet)
		owner.addPet(pet);

		// Set ID after adding to simulate persisted pet
		pet.setId(1);

		// Test JML postcondition: result matches id or is null
		Pet foundPet = owner.getPet(1);
		assertThat(foundPet).isNotNull();
		assertThat(foundPet.getId()).isEqualTo(1);

		// Test with non-existent id
		Pet notFound = owner.getPet(999);
		assertThat(notFound).isNull();
	}

	@Test
	void testGetPetWithIgnoreNewFlag() {
		// Add new pet (no ID)
		owner.addPet(pet);

		// Test with ignoreNew = false (should find new pets)
		Pet foundNew = owner.getPet("Buddy", false);
		assertThat(foundNew).isNotNull();

		// Test with ignoreNew = true (should not find new pets)
		Pet ignoredNew = owner.getPet("Buddy", true);
		assertThat(ignoredNew).isNull();

		// Set ID and test again
		pet.setId(1);
		Pet foundExisting = owner.getPet("Buddy", true);
		assertThat(foundExisting).isNotNull();
	}

	@Test
	void testAddVisitJMLSpecification() {
		// Add pet first, then set ID to simulate persisted pet
		owner.addPet(pet);
		pet.setId(1);

		Visit visit = new Visit();
		visit.setDate(LocalDate.now());
		visit.setDescription("Regular checkup");

		int initialVisitCount = pet.getVisits().size();

		// Test JML preconditions and postconditions
		owner.addVisit(1, visit);

		assertThat(pet.getVisits()).contains(visit);
		assertThat(pet.getVisits().size()).isEqualTo(initialVisitCount + 1);
	}

}