package org.springframework.samples.petclinic;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.vet.VetRepository;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark for PetClinic application performance testing Tests the most demanding
 * database and service layer operations
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
public class PetClinicBenchmark {

	private ConfigurableApplicationContext context;

	private OwnerRepository ownerRepository;

	private VetRepository vetRepository;

	@Setup
	public void setup() {
		context = SpringApplication.run(PetClinicApplication.class);
		ownerRepository = context.getBean(OwnerRepository.class);
		vetRepository = context.getBean(VetRepository.class);
	}

	@TearDown
	public void tearDown() {
		if (context != null) {
			context.close();
		}
	}

	/**
	 * Benchmark: Find owner by last name (database query performance)
	 */
	@Benchmark
	public Page<Owner> benchmarkFindOwnerByLastName() {
		return ownerRepository.findByLastNameStartingWith("Davis", PageRequest.of(0, 10));
	}

	/**
	 * Benchmark: Find owner by ID with pets (entity mapping performance)
	 */
	@Benchmark
	public Owner benchmarkFindOwnerById() {
		return ownerRepository.findById(1).orElse(null);
	}

	/**
	 * Benchmark: Find all vets (database query + collection performance)
	 */
	@Benchmark
	public Collection benchmarkFindAllVets() {
		return vetRepository.findAll();
	}

	/**
	 * Benchmark: Save owner operation (write performance)
	 */
	@Benchmark
	public void benchmarkSaveOwner() {
		Owner owner = new Owner();
		owner.setFirstName("Performance");
		owner.setLastName("Test");
		owner.setAddress("123 Benchmark St");
		owner.setCity("TestCity");
		owner.setTelephone("1234567890");
		ownerRepository.save(owner);
	}

	public static void main(String[] args) throws Exception {
		Options opt = new OptionsBuilder().include(PetClinicBenchmark.class.getSimpleName()).build();
		new Runner(opt).run();
	}

}
