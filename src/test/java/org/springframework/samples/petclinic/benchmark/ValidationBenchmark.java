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
package org.springframework.samples.petclinic.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.PetValidator;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark for validation operations
 *
 * This benchmark tests the performance of Bean Validation (JSR-303) and custom validation
 * logic used in the Pet Clinic application.
 *
 * @author Software Dependability Team
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
public class ValidationBenchmark {

	private Validator beanValidator;

	private PetValidator petValidator;

	private Owner validOwner;

	private Owner invalidOwner;

	private Pet validPet;

	private Pet invalidPet;

	@Setup(Level.Trial)
	public void setup() {
		// Initialize Bean Validation
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		beanValidator = factory.getValidator();
		petValidator = new PetValidator();

		// Setup valid owner
		validOwner = new Owner();
		validOwner.setFirstName("John");
		validOwner.setLastName("Doe");
		validOwner.setAddress("123 Main Street");
		validOwner.setCity("Anytown");
		validOwner.setTelephone("1234567890");

		// Setup invalid owner (missing required fields)
		invalidOwner = new Owner();
		invalidOwner.setFirstName("Jane");
		// Missing lastName, address, city, telephone

		// Setup valid pet
		validPet = new Pet();
		validPet.setName("Max");
		validPet.setBirthDate(LocalDate.now().minusYears(2));
		PetType dogType = new PetType();
		dogType.setName("dog");
		validPet.setType(dogType);

		// Setup invalid pet
		invalidPet = new Pet();
		// Missing name, birthDate, type
	}

	/**
	 * Benchmark: Bean Validation for valid Owner Tests JSR-303 validation performance on
	 * valid objects
	 */
	@Benchmark
	public void benchmarkValidOwnerValidation(Blackhole blackhole) {
		Set<ConstraintViolation<Owner>> violations = beanValidator.validate(validOwner);
		blackhole.consume(violations);
	}

	/**
	 * Benchmark: Bean Validation for invalid Owner Tests JSR-303 validation performance
	 * when violations are found
	 */
	@Benchmark
	public void benchmarkInvalidOwnerValidation(Blackhole blackhole) {
		Set<ConstraintViolation<Owner>> violations = beanValidator.validate(invalidOwner);
		blackhole.consume(violations);
	}

	/**
	 * Benchmark: Custom Pet Validation (valid pet) Tests performance of Spring custom
	 * validation logic
	 */
	@Benchmark
	public void benchmarkValidPetCustomValidation(Blackhole blackhole) {
		Errors errors = new BeanPropertyBindingResult(validPet, "pet");
		petValidator.validate(validPet, errors);
		blackhole.consume(errors.hasErrors());
	}

	/**
	 * Benchmark: Custom Pet Validation (invalid pet) Tests performance when custom
	 * validation finds errors
	 */
	@Benchmark
	public void benchmarkInvalidPetCustomValidation(Blackhole blackhole) {
		Errors errors = new BeanPropertyBindingResult(invalidPet, "pet");
		petValidator.validate(invalidPet, errors);
		blackhole.consume(errors.hasErrors());
	}

	/**
	 * Benchmark: Combined Bean + Custom Validation Tests realistic validation scenario
	 * with both JSR-303 and custom validation
	 */
	@Benchmark
	public void benchmarkCombinedValidation(Blackhole blackhole) {
		// Bean validation first
		Set<ConstraintViolation<Pet>> violations = beanValidator.validate(validPet);

		// Custom validation second
		Errors errors = new BeanPropertyBindingResult(validPet, "pet");
		petValidator.validate(validPet, errors);

		blackhole.consume(violations);
		blackhole.consume(errors);
	}

	/**
	 * Benchmark: Validation of multiple objects in batch Tests performance when
	 * validating collections of objects
	 */
	@Benchmark
	public void benchmarkBatchValidation(Blackhole blackhole) {
		Owner[] owners = { validOwner, invalidOwner, validOwner, invalidOwner, validOwner };

		for (Owner owner : owners) {
			Set<ConstraintViolation<Owner>> violations = beanValidator.validate(owner);
			blackhole.consume(violations);
		}
	}

}