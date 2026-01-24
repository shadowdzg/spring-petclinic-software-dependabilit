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
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark for search operations
 *
 * This benchmark tests the performance of various search scenarios that users commonly
 * perform in the Pet Clinic application.
 *
 * @author Software Dependability Team
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = { "-Xmx2048m" })
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
public class SearchPerformanceBenchmark {

	private ConfigurableApplicationContext context;

	private OwnerRepository ownerRepository;

	private VetRepository vetRepository;

	// Various search patterns to test
	private String[] commonLastNames = { "Davis", "Franklin", "McTavish", "Coleman", "Schroeder" };

	private String[] partialSearches = { "Da", "Fr", "Mc", "Co", "Sc" };

	private String[] singleLetterSearches = { "D", "F", "M", "C", "S" };

	@Setup(Level.Trial)
	public void setupSpringContext() {
		System.setProperty("spring.profiles.active", "h2");
		context = SpringApplication.run(PetClinicApplication.class, "--spring.jpa.hibernate.ddl-auto=create-drop");
		ownerRepository = context.getBean(OwnerRepository.class);
		vetRepository = context.getBean(VetRepository.class);
	}

	@TearDown(Level.Trial)
	public void closeSpringContext() {
		if (context != null) {
			context.close();
		}
	}

	/**
	 * Benchmark: Exact name search Tests performance of searching for complete last names
	 */
	@Benchmark
	public void benchmarkExactNameSearch(Blackhole blackhole) {
		for (String lastName : commonLastNames) {
			Pageable pageable = PageRequest.of(0, 10);
			Page<Owner> owners = ownerRepository.findByLastNameStartingWith(lastName, pageable);
			blackhole.consume(owners.getContent().size());
		}
	}

	/**
	 * Benchmark: Partial name search Tests performance of prefix-based searches (more
	 * common user behavior)
	 */
	@Benchmark
	public void benchmarkPartialNameSearch(Blackhole blackhole) {
		for (String partial : partialSearches) {
			Pageable pageable = PageRequest.of(0, 10);
			Page<Owner> owners = ownerRepository.findByLastNameStartingWith(partial, pageable);
			blackhole.consume(owners.getContent().size());
		}
	}

	/**
	 * Benchmark: Single letter search Tests worst-case scenario for search performance
	 * (broadest results)
	 */
	@Benchmark
	public void benchmarkSingleLetterSearch(Blackhole blackhole) {
		for (String letter : singleLetterSearches) {
			Pageable pageable = PageRequest.of(0, 10);
			Page<Owner> owners = ownerRepository.findByLastNameStartingWith(letter, pageable);
			blackhole.consume(owners.getContent().size());
		}
	}

	/**
	 * Benchmark: Empty search (find all) Tests performance when users search without
	 * criteria
	 */
	@Benchmark
	public void benchmarkEmptySearch(Blackhole blackhole) {
		Pageable pageable = PageRequest.of(0, 20);
		Page<Owner> owners = ownerRepository.findByLastNameStartingWith("", pageable);
		blackhole.consume(owners.getTotalElements());
	}

	/**
	 * Benchmark: Paginated search navigation Tests performance when users navigate
	 * through result pages
	 */
	@Benchmark
	public void benchmarkPaginatedNavigation(Blackhole blackhole) {
		String searchTerm = "";
		int pageSize = 5;

		// Simulate navigating through first 3 pages
		for (int page = 0; page < 3; page++) {
			Pageable pageable = PageRequest.of(page, pageSize);
			Page<Owner> owners = ownerRepository.findByLastNameStartingWith(searchTerm, pageable);
			blackhole.consume(owners.getContent());
		}
	}

	/**
	 * Benchmark: Vet search operations Tests performance of veterinarian listing
	 * (typically smaller dataset)
	 */
	@Benchmark
	public void benchmarkVetSearch(Blackhole blackhole) {
		Collection<Vet> vets = vetRepository.findAll();
		blackhole.consume(vets.size());
	}

	/**
	 * Benchmark: Case-insensitive search simulation Tests performance impact of different
	 * case variations
	 */
	@Benchmark
	public void benchmarkCaseVariations(Blackhole blackhole) {
		String baseName = "davis";
		String[] variations = { baseName, baseName.toUpperCase(), capitalize(baseName), mixedCase(baseName) };

		for (String variation : variations) {
			Pageable pageable = PageRequest.of(0, 10);
			Page<Owner> owners = ownerRepository.findByLastNameStartingWith(variation, pageable);
			blackhole.consume(owners.getContent().size());
		}
	}

	private String capitalize(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	private String mixedCase(String str) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			if (i % 2 == 0) {
				sb.append(Character.toUpperCase(str.charAt(i)));
			}
			else {
				sb.append(Character.toLowerCase(str.charAt(i)));
			}
		}
		return sb.toString();
	}

}