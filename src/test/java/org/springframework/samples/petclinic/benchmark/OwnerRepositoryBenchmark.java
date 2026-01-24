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
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.PetClinicApplication;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark for OwnerRepository operations
 *
 * This benchmark tests the performance of database operations that are critical to the
 * Pet Clinic application.
 *
 * @author Software Dependability Team
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = { "-Xmx2048m" })
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
public class OwnerRepositoryBenchmark {

	private ConfigurableApplicationContext context;

	private OwnerRepository ownerRepository;

	// Test data IDs that exist in the default dataset
	private static final int EXISTING_OWNER_ID = 1;

	private static final String SEARCH_LASTNAME = "Davis";

	private static final String PARTIAL_LASTNAME = "Dav";

	@Setup(Level.Trial)
	public void setupSpringContext() {
		// Start Spring Boot application context for benchmarking
		System.setProperty("spring.profiles.active", "h2");
		context = SpringApplication.run(PetClinicApplication.class, "--spring.jpa.hibernate.ddl-auto=create-drop");
		ownerRepository = context.getBean(OwnerRepository.class);
	}

	@TearDown(Level.Trial)
	public void closeSpringContext() {
		if (context != null) {
			context.close();
		}
	}

	/**
	 * Benchmark: Finding owner by ID Tests the performance of primary key lookups
	 */
	@Benchmark
	public void benchmarkFindById(Blackhole blackhole) {
		Optional<Owner> owner = ownerRepository.findById(EXISTING_OWNER_ID);
		blackhole.consume(owner);
	}

	/**
	 * Benchmark: Finding owners by last name (exact match) Tests the performance of
	 * indexed string queries
	 */
	@Benchmark
	public void benchmarkFindByLastName(Blackhole blackhole) {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Owner> owners = ownerRepository.findByLastNameStartingWith(SEARCH_LASTNAME, pageable);
		blackhole.consume(owners);
	}

	/**
	 * Benchmark: Finding owners by partial last name Tests the performance of partial
	 * string matching (LIKE queries)
	 */
	@Benchmark
	public void benchmarkFindByPartialLastName(Blackhole blackhole) {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Owner> owners = ownerRepository.findByLastNameStartingWith(PARTIAL_LASTNAME, pageable);
		blackhole.consume(owners);
	}

	/**
	 * Benchmark: Paginated search with larger page size Tests the performance impact of
	 * larger result sets
	 */
	@Benchmark
	public void benchmarkPaginatedSearch(Blackhole blackhole) {
		Pageable largePage = PageRequest.of(0, 50);
		Page<Owner> owners = ownerRepository.findByLastNameStartingWith("", largePage);
		blackhole.consume(owners);
	}

	/**
	 * Benchmark: Finding all owners Tests the performance of full table scans
	 */
	@Benchmark
	public void benchmarkFindAll(Blackhole blackhole) {
		Iterable<Owner> allOwners = ownerRepository.findAll();
		blackhole.consume(allOwners);
	}

}